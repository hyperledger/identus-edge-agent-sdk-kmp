package io.iohk.atala.prism.sampleapp.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationSubmission
import io.ktor.util.decodeBase64String
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MessagesAdapter(private var data: MutableList<Message> = mutableListOf()) :
    RecyclerView.Adapter<MessagesAdapter.MessageHolder>() {

    fun updateMessages(updatedMessages: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return updatedMessages.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].id == updatedMessages[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == updatedMessages[newItemPosition]
            }
        })
        data.clear()
        data.addAll(updatedMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.placeholder_message, parent, false)
        return MessageHolder(view)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        // Bind data to the views
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        // Return the size of your data list
        return data.size
    }

    inner class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val type: TextView = itemView.findViewById(R.id.message_type)
        private val body: TextView = itemView.findViewById(R.id.message)

        @OptIn(DelicateCoroutinesApi::class)
        fun bind(message: Message) {
            type.text = message.piuri
            if (message.piuri == ProtocolType.DidcommPresentation.value) {
                GlobalScope.launch {
                    val sdk = Sdk.getInstance()
                    if (sdk.agent.handlePresentationSubmission(message)) {
                        val attachmentData = message.attachments.first().data
                        if (attachmentData::class == AttachmentBase64::class) {
                            attachmentData as AttachmentBase64
                            val decoded = attachmentData.base64.decodeBase64String()
                            val presentationSubmission = Json.decodeFromString<PresentationSubmission>(decoded)
                            presentationSubmission.verifiableCredential.forEach {
                                println("stop")
                            }
                        }
                    } else {
                        // TODO: Change UI body to say invalid presentation
                    }
                }
                this.body.text = message.body
            }
        }
    }
}
