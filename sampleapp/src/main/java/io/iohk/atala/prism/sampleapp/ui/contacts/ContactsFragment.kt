package io.iohk.atala.prism.sampleapp.ui.contacts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.sampleapp.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val viewModel: ContactsViewModel by viewModels()

    private val binding get() = _binding!!
    private val adapter = ContactsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        binding.connections.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStreamObservers()
        binding.addContact.setOnClickListener {
            showAddDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStreamObservers() {
        viewModel.contactsStream().observe(this.viewLifecycleOwner) { contacts ->
            adapter.updateContacts(contacts)
        }
    }

    private fun showAddDialog() {
        val alertDialogBuilder = AlertDialog.Builder(context)

        alertDialogBuilder.setTitle("Out of band connection")

        val editText = EditText(context)
        editText.hint = getString(R.string.enter_oob)

        alertDialogBuilder.setView(editText)

        alertDialogBuilder.setPositiveButton("Validate") { dialog, _ ->
            if (viewModel.isValidURL(editText.text.toString())) {
                Snackbar.make(
                    binding.root,
                    "Valid OOB",
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.parseAndAcceptOOB(editText.text.toString())
                dialog.dismiss()
            } else {
                Snackbar.make(
                    binding.root,
                    "OOB not valid",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactsFragment {
            return ContactsFragment()
        }
    }
}
