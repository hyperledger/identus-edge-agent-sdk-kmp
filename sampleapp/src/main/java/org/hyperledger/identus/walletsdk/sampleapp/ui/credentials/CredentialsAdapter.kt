package org.hyperledger.identus.walletsdk.ui.credentials

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.W3CCredential
import org.hyperledger.identus.walletsdk.sampleapp.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CredentialsAdapter(private var data: MutableList<Credential> = mutableListOf()) :
    RecyclerView.Adapter<CredentialsAdapter.CredentialHolder>() {

    fun updateCredentials(updatedCredentials: List<Credential>) {
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
        private val type: TextView = itemView.findViewById(R.id.credential_id)
        private val issuanceDate: TextView = itemView.findViewById(R.id.credential_issuance_date)
        private val revoked: TextView = itemView.findViewById(R.id.revoked)
        private val typeString: String = itemView.context.getString(R.string.credential_type)
        private val issuanceString: String = itemView.context.getString(R.string.credential_issuance)
        private val expirationString: String = itemView.context.getString(R.string.credential_expiration)

        fun bind(cred: Credential) {
            when (cred::class) {
                JWTCredential::class -> {
                    val jwt = cred as JWTCredential
                    if (jwt.revoked != null && jwt.revoked!!) {
                        revoked.visibility = View.VISIBLE
                    }
                    type.text = String.format(typeString, "JWT")
                    // TODO: Check what else to display
                    jwt.nbf?.let {
                        issuanceDate.text = formatTimeStamp(Instant.ofEpochMilli(it * 1000))
                    }
                }

                W3CCredential::class -> {
                    val w3c = cred as W3CCredential
                    type.text = String.format(typeString, "W3C")
                }

                AnonCredential::class -> {
                    val anon = cred as AnonCredential
                    type.text = String.format(typeString, "Anoncred")
                    issuanceDate.text = String.format("Issuer: ${anon.credentialDefinitionID}")
                }
            }
        }
    }

    private fun formatTimeStamp(instant: Instant): String {
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }
}
