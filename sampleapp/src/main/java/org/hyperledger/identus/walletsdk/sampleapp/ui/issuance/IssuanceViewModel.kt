package org.hyperledger.identus.walletsdk.sampleapp.ui.issuance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.openid4vci.AuthorizationRequestPrepared
import eu.europa.ec.eudi.openid4vci.Issuer
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.sampleapp.Sdk

class IssuanceViewModel(application: Application) : AndroidViewModel(application) {

    private val state: MutableLiveData<AuthorizationRequest> = MutableLiveData()

    fun observeState(): LiveData<AuthorizationRequest> {
        return state
    }

    fun signIn() {
        viewModelScope.launch {
            val sdk = Sdk.getInstance()
            sdk.startAgent("", getApplication())
            val authorizationRequest = sdk
                .oidcAgent
                .createAuthorizationRequest(
                    clientId = "alice-wallet",
                    redirectUri = "edgeagentsdk://oidc.login",
                    offer = Sdk.getInstance()
                        .oidcAgent
                        .parseCredentialOffer("openid-credential-offer://?credential_offer=%7B%22credential_issuer%22%3A%22http%3A%2F%2F192.168.68.113%3A8090%2Foid4vci%2Fissuers%2F13ac8bde-9f01-4570-b858-646c3cb243d1%22%2C%22credential_configuration_ids%22%3A%5B%22StudentProfile%22%5D%2C%22grants%22%3A%7B%22authorization_code%22%3A%7B%22issuer_state%22%3A%22d32e575b-d482-448d-a773-a0e9a7fde193%22%7D%7D%7D")
                )
            state.postValue(AuthorizationRequest(authorizationRequest.first, authorizationRequest.second))
        }
    }
}

data class AuthorizationRequest(
    val issuer: Issuer,
    val authorizationRequestPrepared: AuthorizationRequestPrepared
)
