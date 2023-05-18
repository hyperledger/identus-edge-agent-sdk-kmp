package io.iohk.atala.prism.sampleapp.ui.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import kotlinx.coroutines.launch

class CredentialsViewModel(application: Application) : AndroidViewModel(application) {

    private var credentials: MutableLiveData<List<VerifiableCredential>> = MutableLiveData()

    init {
        viewModelScope.launch {
            Sdk.getInstance(application).pluto?.getAllCredentials()?.collect {
                credentials.postValue(it)
            }
        }
    }

    fun credentialsStream(): LiveData<List<VerifiableCredential>> {
        return credentials
    }
}
