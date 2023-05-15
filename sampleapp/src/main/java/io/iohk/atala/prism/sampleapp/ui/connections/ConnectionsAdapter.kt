package io.iohk.atala.prism.sampleapp.ui.connections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair

class ConnectionsAdapter(private var data: MutableList<DIDPair> = mutableListOf()) :
    RecyclerView.Adapter<ConnectionsAdapter.ConnectionHolder>() {

    fun updateConnections(updatedCredentials: List<DIDPair>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return updatedCredentials.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].host == updatedCredentials[newItemPosition].host &&
                    data[oldItemPosition].receiver == updatedCredentials[newItemPosition].receiver
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == updatedCredentials[newItemPosition]
            }
        })
        data.clear()
        data.addAll(updatedCredentials)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.placeholder_connection, parent, false)
        return ConnectionHolder(view)
    }

    override fun onBindViewHolder(holder: ConnectionHolder, position: Int) {
        // Bind data to the views
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        // Return the size of your data list
        return data.size
    }

    inner class ConnectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val id: TextView
        private val host: TextView
        private val receiver: TextView

        init {
            id = itemView.findViewById(R.id.connection_id)
            host = itemView.findViewById(R.id.connection_host)
            receiver = itemView.findViewById(R.id.connection_receiver)
        }

        fun bind(connection: DIDPair) {
            id.text = connection.name
            host.text = connection.host.toString()
            receiver.text = connection.receiver.toString()
        }
    }
}
