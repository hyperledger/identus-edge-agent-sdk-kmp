package org.hyperledger.identus.walletsdk.ui.messages

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.sampleapp.databinding.CredentialDialogBinding
import org.hyperledger.identus.walletsdk.sampleapp.databinding.FragmentMessagesBinding
import org.hyperledger.identus.walletsdk.sampleapp.ui.messages.InitiateVerificationDialogFragment
import org.hyperledger.identus.walletsdk.sampleapp.ui.messages.MessagesAdapter
import org.hyperledger.identus.walletsdk.sampleapp.ui.messages.MessagesViewModel
import org.hyperledger.identus.walletsdk.sampleapp.ui.messages.UiMessage

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val viewModel: MessagesViewModel by viewModels()

    private val binding get() = _binding!!

    interface ValidateMessageListener {
        fun validateMessage(message: UiMessage)
    }

    private lateinit var adapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sendMessage.setOnClickListener {
            viewModel.sendMessage()
        }
        binding.sendVerification.setOnClickListener {
            InitiateVerificationDialogFragment(viewModel).show(
                parentFragmentManager,
                "InitiateVerificationDialogFragment"
            )
        }

        adapter = MessagesAdapter(
            validateListener = object : ValidateMessageListener {
                override fun validateMessage(message: UiMessage) {
                    viewModel.handlePresentation(message).observe(viewLifecycleOwner) { status ->
                        adapter.updateMessageStatus(
                            UiMessage(
                                id = message.id,
                                piuri = message.piuri,
                                from = message.from,
                                to = message.to,
                                attachments = message.attachments,
                                status = status
                            )
                        )
                    }
                }
            }
        )
        binding.list.adapter = adapter
        setupStreamObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function to show the dialog
    fun showDialogWithOptions(
        credentials: List<Credential>,
        onCredentialSelected: (Credential) -> Unit
    ) {
        val dialogBinding = CredentialDialogBinding.inflate(layoutInflater)

        // Set up the spinner with the options
        context?.let {
            val adapter =
                CustomArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, credentials)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.spinner.adapter = adapter

            // Create and show the dialog
            AlertDialog.Builder(context)
                .setTitle("Choose an Option")
                .setView(dialogBinding.root)
                // Add any other dialog buttons or actions here
                .setPositiveButton("OK") { _, which ->
                    val credential = credentials[dialogBinding.spinner.selectedItemPosition]
                    onCredentialSelected(credential)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun setupStreamObservers() {
        viewModel.messagesStream().observe(this.viewLifecycleOwner) { messages ->
            val uiMessages = messages.map { message ->
                UiMessage(
                    id = message.id,
                    piuri = message.piuri,
                    from = message.from?.toString() ?: "NA",
                    to = message.to?.toString() ?: "NA",
                    attachments = message.attachments
                )
            }
            adapter.updateMessages(uiMessages)
        }
        viewModel.proofRequestToProcess().observe(this.viewLifecycleOwner) { proofRequest ->
            val message = proofRequest.first
            val credentials = proofRequest.second
            showDialogWithOptions(credentials) { credential ->
                viewModel.preparePresentationProof(credential, message)
            }
        }
        viewModel.revokedCredentialsStream()
            .observe(this.viewLifecycleOwner) { revokedCredentials ->
                if (revokedCredentials.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        "Credential revoked ID: ${revokedCredentials.last().id}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(): MessagesFragment {
            return MessagesFragment()
        }
    }

    interface CredentialSelected {
        fun onCredentialSelected(credential: Credential)
    }
}
