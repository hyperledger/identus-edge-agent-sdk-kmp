package io.iohk.atala.prism.sampleapp.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.PrismOnboardingInvitation
import kotlinx.coroutines.launch
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
                        throw PrismAgentError.UnknownInvitationTypeError()
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
