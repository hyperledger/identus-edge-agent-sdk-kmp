package io.iohk.atala.prism.sampleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.iohk.atala.prism.sampleapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val viewModel: FirstViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // TODO:
        // Create Peer and Prism DID
        // Connect with mediator
        // Send and receive message
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStreamObservers()
        binding.createPeerDid.setOnClickListener {
            viewModel.createPeerDid()
        }
        binding.createPrismDid.setOnClickListener {
            viewModel.createPrismDid()
        }
        binding.startAgent.setOnClickListener {
            context?.let { it1 -> viewModel.startAgent(it1) }
//            findNavController().navigate(R.id.action_First2Fragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStreamObservers() {
        viewModel.messageListStream().observe(this.viewLifecycleOwner) { messages ->
//            messages.first {
//            }
        }
        viewModel.notificationListStream().observe(this.viewLifecycleOwner) {
            binding.log.append(it)
        }
    }
}
