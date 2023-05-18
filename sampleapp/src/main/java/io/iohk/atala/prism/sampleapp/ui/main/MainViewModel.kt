package io.iohk.atala.prism.sampleapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.iohk.atala.prism.sampleapp.Sdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val initAgentScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val agentStatusStream: MutableLiveData<String> = MutableLiveData()

    init {
        initAgentScope.launch {
            val sdk = Sdk.getInstance(getApplication())
            val counter = 0
            while (counter < 10 && sdk.agent == null) {
                delay(250)
            }
            sdk.agent?.let {
                sdk.agent?.flowState?.collect {
                    agentStatusStream.postValue(it.name)
                }
            }
        }
    }

    fun agentStatusStream(): LiveData<String> {
        return agentStatusStream
    }
}
