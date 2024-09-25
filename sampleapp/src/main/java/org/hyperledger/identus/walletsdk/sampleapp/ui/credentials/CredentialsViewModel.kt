package org.hyperledger.identus.walletsdk.ui.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.sampleapp.Sdk

class CredentialsViewModel(application: Application) : AndroidViewModel(application) {

    private var credentials: MutableLiveData<List<Credential>> = MutableLiveData()

    fun credentialsStream(): LiveData<List<Credential>> {
        viewModelScope.launch {
            Sdk.getInstance().agent.let {
                it.getAllCredentials().collect { list ->
                    credentials.postValue(list)
                }
            }
        }
        return credentials
    }

    fun isCredentialRevoked(credential: Credential) {
        viewModelScope.launch {
            Sdk.getInstance().agent.isCredentialRevoked(credential)
        }
    }
}
