package io.iohk.atala.prism.sampleapp.ui.messages

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.sampleapp.databinding.CredentialDialogBinding
import io.iohk.atala.prism.sampleapp.databinding.FragmentMessagesBinding
import io.iohk.atala.prism.walletsdk.domain.models.Credential

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val viewModel: MessagesViewModel by viewModels()

    private val binding get() = _binding!!
    private val adapter = MessagesAdapter()

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
        binding.list.adapter = adapter
        binding.sendMessage.setOnClickListener {
            viewModel.sendMessage()
        }
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
            val adapter = CustomArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, credentials)
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
            adapter.updateMessages(messages)
        }
        viewModel.proofRequestToProcess().observe(this.viewLifecycleOwner) { proofRequest ->
            val message = proofRequest.first
            val credentials = proofRequest.second
            showDialogWithOptions(credentials) { credential ->
                viewModel.preparePresentationProof(credential, message)
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
