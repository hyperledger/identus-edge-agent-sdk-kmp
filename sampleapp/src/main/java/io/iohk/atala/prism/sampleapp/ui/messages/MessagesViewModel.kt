package io.iohk.atala.prism.sampleapp.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.DIDCOMM1
import io.iohk.atala.prism.walletsdk.prismagent.DIDCOMM_MESSAGING
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.IssueCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private var messages: MutableLiveData<List<Message>> = MutableLiveData()
    private var didOfferDone = false
    private var presentationDone = false

    init {
        viewModelScope.launch {
            Sdk.getInstance(getApplication()).agent?.let {
                it.handleReceivedMessagesEvents().collect { list ->
                    messages.postValue(list)
                    processMessages(list)
                }
            }
        }
    }

    fun messagesStream(): LiveData<List<Message>> {
        return messages
    }

    fun sendMessage() {
        CoroutineScope(Dispatchers.Default).launch {
            val sdk = Sdk.getInstance(getApplication())
            val did = sdk.agent?.createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        DIDCOMM1,
                        arrayOf(DIDCOMM_MESSAGING),
                        DIDDocument.ServiceEndpoint(sdk.handler?.mediatorDID.toString())
                    )
                ),
                true
            )
            val time = LocalDateTime.now()
            val message = Message(
                piuri = "https://didcomm.org/basicmessage/2.0/message", // TODO: This should be on ProtocolTypes as an enum
                from = did,
                to = did,
                body = "{\"msg\":\"This is a new test message ${time}\"}"
            )
            sdk.mercury?.sendMessage(message)
        }
    }

    private fun processMessages(messages: List<Message>) {
        val sdk = Sdk.getInstance(getApplication())
        messages.forEach { message ->
            sdk.agent?.let { agent ->
                sdk.pluto?.let { pluto ->
                    sdk.mercury?.let { mercury ->
                        if (message.piuri == ProtocolType.DidcommOfferCredential.value && !didOfferDone) {
                            didOfferDone = true
                            viewModelScope.launch {
                                val credentials = pluto.getAllCredentials().first()
                                if (credentials.isEmpty()) {
                                    val offer = OfferCredential.fromMessage(message)
                                    val subjectDID = agent.createNewPrismDID()
                                    val request =
                                        agent.prepareRequestCredentialWithIssuer(
                                            subjectDID,
                                            offer
                                        )
                                    mercury.sendMessage(request.makeMessage())
                                }
                            }
                        }

                        if (message.piuri == ProtocolType.DidcommIssueCredential.value) {
                            agent.processIssuedCredentialMessage(
                                IssueCredential.fromMessage(
                                    message
                                )
                            )
                        }

                        if (message.piuri == ProtocolType.DidcommRequestPresentation.value && !presentationDone) {
                            viewModelScope.launch {
                                presentationDone = true
                                val credential = pluto.getAllCredentials().first().first()
                                val presentation = agent.preparePresentationForRequestProof(
                                    RequestPresentation.fromMessage(message),
                                    credential
                                )
                                mercury.sendMessage(presentation.makeMessage())
                            }
                        }
                    }
                }
            }
        }
    }
}
