package org.hyperledger.identus.walletsdk.sampleapp.ui.messages

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import org.hyperledger.identus.walletsdk.sampleapp.R

class InitiateVerificationDialogFragment(
    private val viewModel: MessagesViewModel
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        // Inflate and set the layout for the dialog
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_initiate_verification, null)

        // Set up EditText
        val toDID = view.findViewById<EditText>(R.id.to)

        // Set up the buttons
        builder.setView(view)
            .setPositiveButton("Accept") { _, _ ->
                if (toDID.text.isNotBlank()) {
//                    viewModel.sendMessage(DID(toDID.text.toString()))
                    viewModel.sendVerificationRequest(toDID.text.toString())
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                this.dismiss()
            }

        return builder.create()
    }
}
