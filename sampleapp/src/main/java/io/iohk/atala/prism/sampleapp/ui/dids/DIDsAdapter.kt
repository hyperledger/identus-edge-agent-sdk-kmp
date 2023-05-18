package io.iohk.atala.prism.sampleapp.ui.dids

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.walletsdk.domain.models.DID

class DIDsAdapter(private var data: MutableList<DID> = mutableListOf()) :
    RecyclerView.Adapter<DIDsAdapter.DIDHolder>() {

    fun updateDIDs(updatedDIDs: List<DID>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return updatedDIDs.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == updatedDIDs[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == updatedDIDs[newItemPosition]
            }
        })
        data.clear()
        data.addAll(updatedDIDs)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DIDHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.placeholder_did, parent, false)
        return DIDHolder(view)
    }

    override fun onBindViewHolder(holder: DIDHolder, position: Int) {
        // Bind data to the views
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        // Return the size of your data list
        return data.size
    }

    inner class DIDHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val did: TextView

        init {
            did = itemView.findViewById(R.id.did)
        }

        fun bind(did: DID) {
            this.did.text = did.toString()
        }
    }
}
