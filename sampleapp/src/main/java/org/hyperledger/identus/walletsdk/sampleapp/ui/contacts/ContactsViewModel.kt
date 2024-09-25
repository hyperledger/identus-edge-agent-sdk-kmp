package org.hyperledger.identus.walletsdk.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.ConnectionlessCredentialOffer
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.ConnectionlessRequestPresentation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.PrismOnboardingInvitation
import org.hyperledger.identus.walletsdk.sampleapp.Sdk
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val contactsStream: MutableLiveData<List<DIDPair>> = MutableLiveData()

    init {
        viewModelScope.launch {
            val pluto = Sdk.getInstance().pluto
            pluto.getAllDidPairs().collect {
                contactsStream.postValue(it)
            }
        }
    }

    fun contactsStream(): LiveData<List<DIDPair>> {
        return contactsStream
    }

    @Throws(EdgeAgentError.UnknownInvitationTypeError::class, Exception::class)
    fun parseAndAcceptOOB(oobUrl: String) {
        Sdk.getInstance().agent.let { agent ->
            viewModelScope.launch {
                try {
                    when (val invitation = agent.parseInvitation(oobUrl)) {
                        is OutOfBandInvitation -> {
                            agent.acceptOutOfBandInvitation(invitation)
                        }

                        is PrismOnboardingInvitation -> {
                            agent.acceptInvitation(invitation)
                        }

                        is ConnectionlessCredentialOffer -> {
                            val offer = OfferCredential.fromMessage(invitation.offerCredential.makeMessage())
                            val subjectDID = agent.createNewPrismDID()
                            val request =
                                agent.prepareRequestCredentialWithIssuer(
                                    subjectDID,
                                    offer
                                )
                            agent.sendMessage(request.makeMessage())
                        }

                        is ConnectionlessRequestPresentation -> {
                            agent.pluto.storeMessage(invitation.requestPresentation.makeMessage())
                        }

                        else -> {
                            throw EdgeAgentError.UnknownInvitationTypeError(invitation.toString())
                        }
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                }
            }
        }
    }

    fun isValidURL(url: String): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: MalformedURLException) {
            false
        } catch (e: URISyntaxException) {
            false
        }
    }
}
