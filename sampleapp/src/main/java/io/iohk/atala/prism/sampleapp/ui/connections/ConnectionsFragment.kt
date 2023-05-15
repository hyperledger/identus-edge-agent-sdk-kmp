package io.iohk.atala.prism.sampleapp.ui.connections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.iohk.atala.prism.sampleapp.databinding.FragmentConnectionsBinding

class ConnectionsFragment : Fragment() {

    private var _binding: FragmentConnectionsBinding? = null
    private val viewModel: ConnectionsViewModel by viewModels()

    private val binding get() = _binding!!
    private val adapter = ConnectionsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionsBinding.inflate(inflater, container, false)
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
        viewModel.connectionsStream().observe(this.viewLifecycleOwner) { connections ->
            adapter.updateConnections(connections)
        }
    }

    companion object {
        fun newInstance(): ConnectionsFragment {
            return ConnectionsFragment()
        }
    }
}
