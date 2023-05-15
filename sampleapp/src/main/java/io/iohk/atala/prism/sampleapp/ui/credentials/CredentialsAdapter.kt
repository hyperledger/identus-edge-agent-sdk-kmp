package io.iohk.atala.prism.sampleapp.ui.credentials

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential

class CredentialsAdapter(private var data: MutableList<VerifiableCredential> = mutableListOf()) :
    RecyclerView.Adapter<CredentialsAdapter.CredentialHolder>() {

    fun updateCredentials(updatedCredentials: List<VerifiableCredential>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return updatedCredentials.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].id == updatedCredentials[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == updatedCredentials[newItemPosition]
            }
        })
        data.clear()
        data.addAll(updatedCredentials)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.placeholder_credential, parent, false)
        return CredentialHolder(view)
    }

    override fun onBindViewHolder(holder: CredentialHolder, position: Int) {
        // Bind data to the views
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        // Return the size of your data list
        return data.size
    }

    inner class CredentialHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val id: TextView
        private val title: TextView
        private val date: TextView

        init {
            id = itemView.findViewById(R.id.credential_id)
            title = itemView.findViewById(R.id.credential_title)
            date = itemView.findViewById(R.id.credential_date)
        }

        fun bind(credential: VerifiableCredential) {
            id.text = credential.id
            title.text = credential.credentialSubject
            date.text = credential.issuanceDate
        }
    }
}
