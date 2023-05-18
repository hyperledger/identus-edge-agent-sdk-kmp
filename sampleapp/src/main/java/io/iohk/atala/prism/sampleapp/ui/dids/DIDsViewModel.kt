package io.iohk.atala.prism.sampleapp.ui.dids

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import kotlinx.coroutines.launch

class DIDsViewModel(application: Application) : AndroidViewModel(application) {

    private val didsStream: MutableLiveData<List<DID>> = MutableLiveData()

    init {
        viewModelScope.launch {
            val pluto = Sdk.getInstance(getApplication<Application>()).pluto
            pluto?.getAllPeerDIDs()?.collect { peerDIDs ->
                val dids = peerDIDs.map { it.did }
                didsStream.postValue(dids)
            }
        }
    }

    fun didsStream(): LiveData<List<DID>> {
        return didsStream
    }

    fun createPeerDID() {
        viewModelScope.launch {
            val sdk = Sdk.getInstance(getApplication())
            sdk.agent?.let {
                it.createNewPeerDID(
                    arrayOf(
                        DIDDocument.Service(
                            "#didcomm-1",
                            arrayOf("DIDCommMessaging"),
                            DIDDocument.ServiceEndpoint(sdk.handler?.mediatorDID.toString())
                        )
                    ),
                    true
                )
            }
        }
    }
}
