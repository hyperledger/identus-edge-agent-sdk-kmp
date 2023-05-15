package io.iohk.atala.prism.sampleapp.ui.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential

class CredentialsViewModel(application: Application) : AndroidViewModel(application) {

    private var credentials: MutableLiveData<List<VerifiableCredential>> = MutableLiveData()

    fun credentialsStream(): LiveData<List<VerifiableCredential>> {
        return credentials
    }
}
