package org.hyperledger.identus.walletsdk.ui.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential

class CustomArrayAdapter(
    context: Context,
    resource: Int,
    objects: List<Credential>
) : ArrayAdapter<Credential>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        val credential = getItem(position)
        credential?.let {
            when (credential::class) {
                JWTCredential::class -> {
                    label.text = (it as JWTCredential).properties["schema"].toString()
                }
                AnonCredential::class -> {
                    label.text = (it as AnonCredential).schemaID
                }
            }
        }
        return label
    }
}
