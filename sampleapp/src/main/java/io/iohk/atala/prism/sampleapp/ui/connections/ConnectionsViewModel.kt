package io.iohk.atala.prism.sampleapp.ui.connections

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair

class ConnectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val connectionsStream: MutableLiveData<List<DIDPair>> = MutableLiveData()

    fun connectionsStream(): LiveData<List<DIDPair>> {
        return connectionsStream
    }
}
