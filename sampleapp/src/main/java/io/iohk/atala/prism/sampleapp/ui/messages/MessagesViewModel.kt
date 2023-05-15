package io.iohk.atala.prism.sampleapp.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.iohk.atala.prism.walletsdk.domain.models.Message

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private var messages: MutableLiveData<List<Message>> = MutableLiveData()

    fun messagesStream(): LiveData<List<Message>> {
        return messages
    }
}
