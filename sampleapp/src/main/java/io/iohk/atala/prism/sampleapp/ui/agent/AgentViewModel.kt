package io.iohk.atala.prism.sampleapp.ui.agent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    fun startAgent() {
        val sdk = Sdk.getInstance(getApplication<Application>())
        viewModelScope.launch {
            sdk.startAgent()
        }
    }
}
