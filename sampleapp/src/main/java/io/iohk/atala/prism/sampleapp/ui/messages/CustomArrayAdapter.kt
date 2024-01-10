package io.iohk.atala.prism.sampleapp.ui.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.pollux.models.AnonCredential
import io.iohk.atala.prism.walletsdk.pollux.models.JWTCredential

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
