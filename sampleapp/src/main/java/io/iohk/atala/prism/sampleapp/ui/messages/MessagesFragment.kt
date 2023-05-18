package io.iohk.atala.prism.sampleapp.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.iohk.atala.prism.sampleapp.databinding.FragmentMessagesBinding

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

    private fun setupStreamObservers() {
        viewModel.messagesStream().observe(this.viewLifecycleOwner) { messages ->
            adapter.updateMessages(messages)
        }
    }

    companion object {
        fun newInstance(): MessagesFragment {
            return MessagesFragment()
        }
    }
}
