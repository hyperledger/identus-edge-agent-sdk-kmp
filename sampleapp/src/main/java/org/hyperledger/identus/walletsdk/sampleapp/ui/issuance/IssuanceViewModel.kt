package org.hyperledger.identus.walletsdk.sampleapp.ui.issuance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.openid4vci.AuthorizationRequestPrepared
import eu.europa.ec.eudi.openid4vci.Issuer
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.sampleapp.Sdk
import java.net.URI

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
                        .parseCredentialOffer("openid-credential-offer://?credential_offer=%7B%22credential_issuer%22%3A%22http%3A%2F%2F192.168.68.113%3A8090%2Foid4vci%2Fissuers%2F91b70773-58a3-4e94-9207-0f16e9d9efdc%22%2C%22credential_configuration_ids%22%3A%5B%22StudentProfile%22%5D%2C%22grants%22%3A%7B%22authorization_code%22%3A%7B%22issuer_state%22%3A%22efb0efa5-2b2e-4343-89a2-9844f5f18713%22%7D%7D%7D")
                )
            state.postValue(AuthorizationRequest(authorizationRequest.first, authorizationRequest.second))
        }
    }

    fun handleCallbackUrl(uri: URI) {
        viewModelScope.launch {
            val sdk = Sdk.getInstance()
            state.value?.let { state ->
                sdk.oidcAgent.handleTokenRequest(state.authorizationRequestPrepared, state.issuer, uri)
            }
        }
    }
}

data class AuthorizationRequest(
    val issuer: Issuer,
    val authorizationRequestPrepared: AuthorizationRequestPrepared
)
