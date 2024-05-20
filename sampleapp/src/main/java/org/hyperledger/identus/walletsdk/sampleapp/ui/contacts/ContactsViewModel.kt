package org.hyperledger.identus.walletsdk.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.edgeagent.PrismAgentError
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

    @Throws(PrismAgentError.UnknownInvitationTypeError::class, Exception::class)
    fun parseAndAcceptOOB(oobUrl: String) {
        Sdk.getInstance().agent.let { agent ->
            viewModelScope.launch {
                when (val invitation = agent.parseInvitation(oobUrl)) {
                    is OutOfBandInvitation -> {
                        agent.acceptOutOfBandInvitation(invitation)
                    }

                    is PrismOnboardingInvitation -> {
                        agent.acceptInvitation(invitation)
                    }

                    else -> {
                        throw PrismAgentError.UnknownInvitationTypeError(invitation.toString())
                    }
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
