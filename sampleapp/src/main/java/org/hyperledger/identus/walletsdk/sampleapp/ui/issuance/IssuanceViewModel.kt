package org.hyperledger.identus.walletsdk.sampleapp.ui.issuance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.openid4vci.*
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
                    redirectUri = "localhost:7777",
                    offer = Sdk.getInstance()
                        .oidcAgent
                        .parseCredentialOffer("openid-credential-offer://?credential_offer=%7B%22credential_issuer%22%3A%22http%3A%2F%2F192.168.68.113%3A8090%2Foid4vci%2Fissuers%2Fd849f775-3904-403c-b2e9-1e58a372e533%22%2C%22credential_configuration_ids%22%3A%5B%22StudentProfile%22%5D%2C%22grants%22%3A%7B%22authorization_code%22%3A%7B%22issuer_state%22%3A%22f9f7372b-1b6d-40bf-b7e1-d08d9d7a328c%22%7D%7D%7D")
                )
            state.postValue(AuthorizationRequest(authorizationRequest.first, authorizationRequest.second))
        }
    }
}

data class AuthorizationRequest(
    val issuer: Issuer,
    val authorizationRequestPrepared: AuthorizationRequestPrepared
)
