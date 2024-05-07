package org.hyperledger.identus.walletsdk.ui.agent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.hyperledger.identus.walletsdk.sampleapp.Sdk
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    fun startAgent(mediatorDID: String) {
        val sdk = Sdk.getInstance()
        viewModelScope.launch {
            sdk.startAgent(mediatorDID, getApplication<Application>())
        }
    }
}
