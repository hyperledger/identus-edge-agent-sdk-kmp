package org.hyperledger.identus.walletsdk.ui.dids

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.hyperledger.identus.walletsdk.sampleapp.databinding.FragmentDidsBinding

class DIDsFragment : Fragment() {

    private var _binding: FragmentDidsBinding? = null
    private val viewModel: DIDsViewModel by viewModels()

    private val binding get() = _binding!!
    private val adapter = DIDsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDidsBinding.inflate(inflater, container, false)
        binding.dids.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStreamObservers()
        binding.createDid.setOnClickListener {
            viewModel.createPeerDID()
        }
        binding.createPrismDid.setOnClickListener {
            viewModel.createPrismDID()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStreamObservers() {
        viewModel.didsStream().observe(this.viewLifecycleOwner) { DIDs ->
            adapter.updateDIDs(DIDs)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): DIDsFragment {
            return DIDsFragment()
        }
    }
}
