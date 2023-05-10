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
        // Connect with mediator
        // Send and receive message
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStreamObservers()
        binding.startAgent.setOnClickListener {
            context?.let { it1 -> viewModel.startAgent(it1) }
//            findNavController().navigate(R.id.action_First2Fragment_to_SecondFragment)
        }
        binding.sendMessage.setOnClickListener {
//            viewModel.sendTestMessage()
            viewModel.createPeerDid()
        }
        binding.createPeerDID.setOnClickListener {
            viewModel.createPeerDid()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopAgent()
    }

    private fun setupStreamObservers() {
        viewModel.messageListStream().observe(this.viewLifecycleOwner) { messages ->
            var text = binding.log.text
            messages.forEach { message ->
                var textToAppend = "${message.id}: ${message.body} \n"
                message.attachments.forEach { attachment ->
                    textToAppend += "Attachment ID: ${attachment.id}: \n"
                }
                binding.log.text = "$textToAppend $text"
            }
        }
        viewModel.notificationListStream().observe(this.viewLifecycleOwner) {
            binding.log.append(it)
        }
        viewModel.agentStateStream().observe(this.viewLifecycleOwner) {
            binding.log.append("Agent state: $it \n")
        }
    }
}
