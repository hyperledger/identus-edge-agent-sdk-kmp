package io.iohk.atala.prism.sampleapp.ui.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CredentialsViewModel(application: Application) : AndroidViewModel(application) {

    private var credentials: MutableLiveData<List<Credential>> = MutableLiveData()

    init {
        viewModelScope.launch {
            Sdk.getInstance(application).agent?.let {
                it.getAllCredentials().collect { list ->
                    credentials.postValue(list)
                }
            }
        }
    }

    fun credentialsStream(): LiveData<List<Credential>> {
        return credentials
    }
}
