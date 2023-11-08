package io.iohk.atala.prism.sampleapp.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.iohk.atala.prism.sampleapp.databinding.FragmentAgentBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AgentFragment : Fragment() {

    private var _binding: FragmentAgentBinding? = null
    private val viewModel: AgentViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.startAgent.setOnClickListener {
            val mediatorDID = binding.mediatorDid.text.toString()
            viewModel.startAgent(mediatorDID)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AgentFragment {
            return AgentFragment()
        }
    }
}
