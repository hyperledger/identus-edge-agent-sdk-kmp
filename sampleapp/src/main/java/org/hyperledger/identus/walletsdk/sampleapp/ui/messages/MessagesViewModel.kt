@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.sampleapp.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.db.AppDatabase
import org.hyperledger.identus.walletsdk.db.DatabaseClient
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.SDJWTPresentationClaims
import org.hyperledger.identus.walletsdk.edgeagent.DIDCOMM1
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.IssueCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.sampleapp.Sdk
import org.hyperledger.identus.walletsdk.sampleapp.db.Message as MessageEntity

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private var messages: MutableLiveData<List<Message>> = MutableLiveData()
    private var proofRequestToProcess: MutableLiveData<Pair<Message, List<Credential>>> =
        MutableLiveData()
    private val issuedCredentials: ArrayList<String> = arrayListOf()
    private val processedOffers: ArrayList<String> = arrayListOf()
    private val db: AppDatabase = DatabaseClient.getInstance()
    private val revokedCredentialsNotified: MutableList<Credential> = mutableListOf()
    private var revokedCredentials: MutableLiveData<List<Credential>> = MutableLiveData()
    private var errorLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.messageDao().isMessageRead("")
        }
    }

    private fun insertMessages(list: List<Message>) {
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
                piuri = ProtocolType.BasicMessage.value,
                from = did,
                to = toDID ?: did,
                body = "{\"msg\":\"This is a new test message ${time}\"}"
            )
            sdk.agent.sendMessage(message)
        }
    }

    fun sendVerificationRequest(toDID: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val sdk = Sdk.getInstance()
            // JWT presentation request
//            sdk.agent.initiatePresentationRequest(
//                type = CredentialType.JWT,
//                toDID = DID(toDID),
//                presentationClaims = JWTPresentationClaims(
//                    claims = mapOf(
//                        "emailAddress" to InputFieldFilter(
//                            type = "string",
//                            pattern = "cristian.castro@iohk.io"
//                        )
//                    )
//                ),
//                domain = "domain",
//                challenge = "challenge"
//            )

            // SD-JWT presentation request
            sdk.agent.initiatePresentationRequest(
                type = CredentialType.SDJWT,
                toDID = DID(toDID),
                presentationClaims = SDJWTPresentationClaims(
                    claims = mapOf(
                        "firstName" to InputFieldFilter(
                            type = "string",
                            pattern = "Wonderland"
                        ),
                        "emailAddress" to InputFieldFilter(
                            type = "string",
                            pattern = "alice@wonderland.com"
                        )
                    )
                )
            )

            // Anoncreds presentation request
//            sdk.agent.initiatePresentationRequest(
//                type = CredentialType.ANONCREDS_PROOF_REQUEST,
//                toDID = DID(toDID),
//                presentationClaims = AnoncredsPresentationClaims(
//                    predicates = mapOf(
//                        "0_age" to AnoncredsInputFieldFilter(
//                            type = "string",
//                            name = "age",
//                            gte = 18
//                        )
//                    ),
//                    attributes = mapOf(
//                        "0_name" to RequestedAttributes(
//                            "name",
//                            setOf("name"),
//                            emptyMap(),
//                            null
//                        )
//                    )
//                )
//            )
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
                    if (credential is ProvableCredential) {
                        try {
                            val presentation = agent.preparePresentationForRequestProof(
                                RequestPresentation.fromMessage(message),
                                credential
                            )
                            sdk.agent.sendMessage(presentation.makeMessage())
                        } catch (e: EdgeAgentError.CredentialNotValidForPresentationRequest) {
                            errorLiveData.postValue(e.message)
                        }
                    }
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

    fun handlePresentation(uiMessage: UiMessage): LiveData<String> {
        val liveData = MutableLiveData<String>()
        val handler = CoroutineExceptionHandler { _, exception ->
            liveData.postValue(exception.message)
        }
        viewModelScope.launch(handler) {
            messages.value?.find { it.id == uiMessage.id }?.let { message ->
                val sdk = Sdk.getInstance()
                try {
                    val valid = sdk.agent.handlePresentation(message)
                    if (valid) {
                        liveData.postValue("Valid!")
                    } else {
                        liveData.postValue("Not valid!")
                    }
                } catch (e: Exception) {
                    liveData.postValue("Not valid. ${e.message}")
                }
            }
        }
        return liveData
    }

    fun streamError(): LiveData<String> {
        return errorLiveData
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
                                    if (!processedOffers.contains(it)) {
                                        processedOffers.add(it)
                                        viewModelScope.launch {
                                            val offer = OfferCredential.fromMessage(message)
                                            val subjectDID = agent.createNewPrismDID()

                                            val x = (agent.castor as CastorImpl).resolvers.first().resolve(subjectDID.toString())
                                            val didDoc = agent.castor.resolveDID(subjectDID.toString())
                                            val request =
                                                agent.prepareRequestCredentialWithIssuer(
                                                    subjectDID,
                                                    offer
                                                )
                                            agent.sendMessage(request.makeMessage())
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
