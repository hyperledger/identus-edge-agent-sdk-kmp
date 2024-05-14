package org.hyperledger.identus.walletsdk.ui.credentials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.hyperledger.identus.walletsdk.sampleapp.databinding.FragmentCredentialsBinding

class CredentialsFragment : Fragment() {

    private var _binding: FragmentCredentialsBinding? = null
    private val viewModel: CredentialsViewModel by viewModels()

    private val binding get() = _binding!!
    private val adapter = CredentialsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCredentialsBinding.inflate(inflater, container, false)
        binding.credentials.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStreamObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStreamObservers() {
        viewModel.credentialsStream().observe(this.viewLifecycleOwner) { credentials ->
            adapter.updateCredentials(credentials)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): CredentialsFragment {
            return CredentialsFragment()
        }
    }
}
