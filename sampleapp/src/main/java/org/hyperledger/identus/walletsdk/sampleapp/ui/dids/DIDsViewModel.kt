package org.hyperledger.identus.walletsdk.ui.dids

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.edgeagent.DIDCOMM1
import org.hyperledger.identus.walletsdk.sampleapp.Sdk

class DIDsViewModel(application: Application) : AndroidViewModel(application) {

    private val didsStream: MutableLiveData<List<DID>> = MutableLiveData()

    init {
        viewModelScope.launch {
            val pluto = Sdk.getInstance().pluto
            pluto.getAllPeerDIDs().collect { peerDIDs ->
                val didList = peerDIDs.map { it.did }
                didsStream.postValue(didList)
            }
        }
    }

    fun didsStream(): LiveData<List<DID>> {
        return didsStream
    }

    fun createPeerDID() {
        viewModelScope.launch {
            val sdk = Sdk.getInstance()
            sdk.agent.createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        DIDCOMM1,
                        arrayOf(DIDCOMM_MESSAGING),
                        DIDDocument.ServiceEndpoint(sdk.handler.mediatorDID.toString())
                    )
                ),
                true
            )
        }
    }

    fun createPrismDID() {
        viewModelScope.launch {
            val sdk = Sdk.getInstance()
            val did = sdk.agent.createNewPrismDID(format = "vc+sd-jwt")
        }
    }
}
