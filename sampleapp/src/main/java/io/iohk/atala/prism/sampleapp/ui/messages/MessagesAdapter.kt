package io.iohk.atala.prism.sampleapp.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.walletsdk.domain.models.Message

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
        private val id: TextView
        private val message: TextView
        private val x: TextView

        init {
            id = itemView.findViewById(R.id.message_id)
            message = itemView.findViewById(R.id.message)
            x = itemView.findViewById(R.id.x)
        }

        fun bind(message: Message) {
            id.text = message.id
            this.message.text = message.body
        }
    }
}
