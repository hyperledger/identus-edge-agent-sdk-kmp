package org.hyperledger.identus.walletsdk.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.sampleapp.R

class ContactsAdapter(private var data: MutableList<DIDPair> = mutableListOf()) :
    RecyclerView.Adapter<ContactsAdapter.ContactsHolder>() {

    fun updateContacts(updatedContacts: List<DIDPair>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return updatedContacts.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].holder == updatedContacts[newItemPosition].holder &&
                    data[oldItemPosition].receiver == updatedContacts[newItemPosition].receiver
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == updatedContacts[newItemPosition]
            }
        })
        data.clear()
        data.addAll(updatedContacts)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.placeholder_contact, parent, false)
        return ContactsHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsHolder, position: Int) {
        // Bind data to the views
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        // Return the size of your data list
        return data.size
    }

    inner class ContactsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val host: TextView = itemView.findViewById(R.id.contact_host)
        private val receiver: TextView = itemView.findViewById(R.id.contact_receiver)

        fun bind(contact: DIDPair) {
            host.text = contact.holder.toString()
            receiver.text = contact.receiver.toString()
        }
    }
}
