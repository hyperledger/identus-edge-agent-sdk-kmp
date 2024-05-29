package org.hyperledger.identus.walletsdk.ui.dids

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.sampleapp.R

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
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val dropdown: ImageView = itemView.findViewById(R.id.dropdown)
        private val did: TextView = itemView.findViewById(R.id.did)

        fun bind(did: DID) {
            this.cardView.setOnClickListener { handleDropdownClick() }
            this.dropdown.setOnClickListener {
                handleDropdownClick()
            }
            this.did.text = did.toString()
        }

        private fun handleDropdownClick() {
            var drawable =
                ResourcesCompat.getDrawable(this.dropdown.context.resources, android.R.drawable.arrow_up_float, null)
            if (this.did.maxLines == 1) {
                this.did.maxLines = 3
            } else {
                drawable = ResourcesCompat.getDrawable(
                    this.dropdown.context.resources,
                    android.R.drawable.arrow_down_float,
                    null
                )
                this.did.maxLines = 1
            }
            this.dropdown.setImageDrawable(drawable)
        }
    }
}
