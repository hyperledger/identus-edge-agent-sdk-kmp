package io.iohk.atala.prism.sampleapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import kotlinx.coroutines.launch

class FirstViewModel(application: Application) : AndroidViewModel(application) {

    private val messageList: MutableLiveData<List<Message>> = MutableLiveData(listOf())
    private val notification: MutableLiveData<String> = MutableLiveData("")
    private val agentState: MutableLiveData<String> = MutableLiveData("")

    fun messageListStream(): LiveData<List<Message>> {
        return messageList
    }

    fun notificationListStream(): LiveData<String> {
        return notification
    }

    fun agentStateStream(): LiveData<String> {
        return agentState
    }

    @Throws(Exception::class, PrismAgentError.UnknownInvitationTypeError::class)
    fun parseAndAcceptOOB(oobUrl: String) {
//        if (this::agent.isInitialized.not()) {
//            throw Exception("Agent has not been started")
//        }
//        viewModelScope.launch {
//            when (val invitation = agent.parseInvitation(oobUrl)) {
//                is OutOfBandInvitation -> {
//                    agent.acceptOutOfBandInvitation(invitation)
//                }
//                is PrismOnboardingInvitation -> {
//                    agent.acceptInvitation(invitation)
//                }
//                else -> {
//                    throw PrismAgentError.UnknownInvitationTypeError()
//                }
//            }
//        }
    }

    fun sendTestMessage(did: DID) {
        viewModelScope.launch {
//            val senderPeerDid = agent.createNewPeerDID(
//                emptyArray(),
//                true
//            )
            val message = Message(
                piuri = "https://didcomm.org/basicmessage/2.0/message", // TODO: This should be on ProtocolTypes as an enum
                from = did,
                to = did,
                body = "{\"msg\":\"This is a test message\"}"
            )

            println("Send message")
//            mercury.sendMessage(message)
        }
    }

    fun createPeerDid() {
        viewModelScope.launch {
//            val did = agent.createNewPeerDID(
//                arrayOf(
//                    DIDDocument.Service(
//                        "#didcomm-1",
//                        arrayOf("DIDCommMessaging"),
//                        DIDDocument.ServiceEndpoint(handler.mediatorDID.toString()),
//                    ),
//                ),
//                true
//            )
//            println(did.toString())
//            sendTestMessage(did)
        }
    }
}
