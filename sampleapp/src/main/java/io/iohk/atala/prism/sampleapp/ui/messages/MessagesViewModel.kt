package io.iohk.atala.prism.sampleapp.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.sampleapp.db.AppDatabase
import io.iohk.atala.prism.sampleapp.db.DatabaseClient
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.InputFieldFilter
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PresentationClaims
import io.iohk.atala.prism.walletsdk.domain.models.StringPredicate
import io.iohk.atala.prism.walletsdk.prismagent.DIDCOMM1
import io.iohk.atala.prism.walletsdk.prismagent.DIDCOMM_MESSAGING
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.IssueCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import io.iohk.atala.prism.sampleapp.db.Message as MessageEntity

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private var messages: MutableLiveData<List<Message>> = MutableLiveData()
    private var proofRequestToProcess: MutableLiveData<Pair<Message, List<Credential>>> =
        MutableLiveData()
    private val issuedCredentials: ArrayList<String> = arrayListOf()
    private val processedOffers: ArrayList<String> = arrayListOf()
    private val db: AppDatabase = DatabaseClient.getInstance()
    private val revokedCredentialsNotified: MutableList<Credential> = mutableListOf()
    private var revokedCredentials: MutableLiveData<List<Credential>> = MutableLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.messageDao().isMessageRead("")
        }
    }

    private suspend fun insertMessages(list: List<Message>) {
        list.forEach { msg ->
            db.messageDao()
                .insertMessage(MessageEntity(messageId = msg.id, isRead = false))
        }
    }

    fun messagesStream(): LiveData<List<Message>> {
        viewModelScope.launch(Dispatchers.IO) {
            Sdk.getInstance().agent.let {
                it.handleReceivedMessagesEvents().collect { list ->
                    insertMessages(list)
                    messages.postValue(list)
                    processMessages(list)
                }
            }
        }
        return messages
    }

    fun sendMessage(toDID: DID? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            val sdk = Sdk.getInstance()
            val did = sdk.agent.createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        DIDCOMM1,
                        arrayOf(DIDCOMM_MESSAGING),
                        DIDDocument.ServiceEndpoint(sdk.handler.mediatorDID.toString())
                    )
                ),
                true
            )
            val time = LocalDateTime.now()
            val message = Message(
                // TODO: This should be on ProtocolTypes as an enum
                piuri = "https://didcomm.org/basicmessage/2.0/message",
                from = did,
                to = toDID ?: did,
                body = "{\"msg\":\"This is a new test message ${time}\"}"
            )
            sdk.mercury.sendMessage(message)
        }
    }

    fun sendVerificationRequest(toDID: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val sdk = Sdk.getInstance()
            sdk.agent.initiatePresentationRequest(
                type = CredentialType.JWT,
                toDID = DID(toDID),
                presentationClaims = PresentationClaims(
                    claims = mapOf(
//                        "$.issuer" to InputFieldFilter(
//                            type = "string",
//                            value = "did:prism:8b5321c004d4a11e87ef3601350eaa8b472a546d11677c9c6845c55a490f02f4"
//                        ),
                        "$.vp.emailAddress" to InputFieldFilter(
                            type = "string",
                            pattern = "corporate@tomate.domain.com"
                        )
                    ),
                    issuer = "did:prism:8b5321c004d4a11e87ef3601350eaa8b472a546d11677c9c6845c55a490f02f4"
                ),
                domain = "domain",
                challenge = "challenge"
            )
        }
    }

    fun proofRequestToProcess(): LiveData<Pair<Message, List<Credential>>> {
        return proofRequestToProcess
    }

    fun preparePresentationProof(credential: Credential, message: Message) {
        val sdk = Sdk.getInstance()
        sdk.agent.let { agent ->
            sdk.mercury.let { mercury ->
                viewModelScope.launch {
                    val presentation = agent.preparePresentationForRequestProof(
                        RequestPresentation.fromMessage(message),
                        credential
                    )
                    mercury.sendMessage(presentation.makeMessage())
                }
            }
        }
    }

    fun revokedCredentialsStream(): LiveData<List<Credential>> {
        viewModelScope.launch {
            Sdk.getInstance().agent.let {
                it.observeRevokedCredentials().collect { list ->
                    val newRevokedCredentials = list.filter { newCredential ->
                        revokedCredentialsNotified.none { notifiedCredential ->
                            notifiedCredential.id == newCredential.id
                        }
                    }
                    if (newRevokedCredentials.isNotEmpty()) {
                        revokedCredentialsNotified.addAll(newRevokedCredentials)
                        revokedCredentials.postValue(newRevokedCredentials)
                    } else {
                        revokedCredentials.postValue(emptyList())
                    }
                }
            }
        }
        return revokedCredentials
    }

    private suspend fun processMessages(messages: List<Message>) {
        val sdk = Sdk.getInstance()
        val messageIds: List<String> = messages.map { it.id }
        val messagesReadStatus =
            db.messageDao().areMessagesRead(messageIds).associate { it.messageId to it.isRead }
        messages.forEach { message ->
            if (messagesReadStatus[message.id] == false) {
                sdk.agent.let { agent ->
                    sdk.pluto.let { pluto ->
                        sdk.mercury.let { mercury ->
                            if (message.piuri == ProtocolType.DidcommOfferCredential.value) {
                                message.thid?.let {
                                    println("Processed offers: $it")
                                    if (!processedOffers.contains(it)) {
                                        println("Processing offer: $it")
                                        processedOffers.add(it)
                                        viewModelScope.launch {
//                                val credentials = pluto.getAllCredentials().first()
//                                if (credentials.isEmpty()) {
                                            val offer = OfferCredential.fromMessage(message)
                                            val subjectDID = agent.createNewPrismDID()
                                            val request =
                                                agent.prepareRequestCredentialWithIssuer(
                                                    subjectDID,
                                                    offer
                                                )
                                            mercury.sendMessage(request.makeMessage())
//                                }
                                        }
                                    }
                                }
                            }
                            if (message.piuri == ProtocolType.DidcommIssueCredential.value) {
                                message.thid?.let {
                                    if (!issuedCredentials.contains(it)) {
                                        issuedCredentials.add(it)
                                        viewModelScope.launch {
                                            agent.processIssuedCredentialMessage(
                                                IssueCredential.fromMessage(
                                                    message
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            if (message.piuri == ProtocolType.DidcommRequestPresentation.value && message.direction == Message.Direction.RECEIVED) {
                                viewModelScope.launch {
                                    agent.getAllCredentials().collect {
                                        proofRequestToProcess.postValue(Pair(message, it))
                                    }
                                }
                            }
                        }

                        db.messageDao()
                            .updateMessage(MessageEntity(messageId = message.id, isRead = true))
                    }
                }
            }
        }
    }
}
