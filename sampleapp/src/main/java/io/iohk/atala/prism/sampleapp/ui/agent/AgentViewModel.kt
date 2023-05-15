package io.iohk.atala.prism.sampleapp.ui.agent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.SampleApplication
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val agentStatusStream: MutableLiveData<String> = MutableLiveData()

    fun agentStatusStream(): LiveData<String> {
        return agentStatusStream
    }

    fun startAgent() {
        val app = getApplication<SampleApplication>()
        viewModelScope.launch {
            try {
                val agent = app.getAgent(app.applicationContext)
                agent.flowState.collect {
                    agentStatusStream.postValue(it.name)
                }
            } catch (e: Exception) {
                println("Agent view model start agent")
            }
        }
        viewModelScope.launch {
            app.startAgent()
        }
    }

    fun stopAgent() {
        viewModelScope.launch {
            try {
                val app = getApplication<SampleApplication>()
                app.stopAgent()
            } catch (e: Exception) {
                println("Agent view model stop agent")
            }
        }
    }
}
