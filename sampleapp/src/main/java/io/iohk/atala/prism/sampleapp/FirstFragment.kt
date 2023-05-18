package io.iohk.atala.prism.sampleapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.iohk.atala.prism.sampleapp.databinding.FragmentFirstBinding
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val viewModel: FirstViewModel by viewModels()
    private lateinit var logger: TextFieldLogger

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
        logger = TextFieldLogger(binding.log)
        binding.oobField.editText?.setText("https://my.domain.com/path?_oob=eyJpZCI6Ijc2ODkxMjNhLTA3MTgtNDJjYS1iMTNhLTRmMzAxNWJlMjNkMSIsInR5cGUiOiJodHRwczovL2RpZGNvbW0ub3JnL291dC1vZi1iYW5kLzIuMC9pbnZpdGF0aW9uIiwiZnJvbSI6ImRpZDpwZWVyOjIuRXo2TFN0QUJ0MXJqMm1wZURmSlZMeVhQQ1FaaUZXSEdoOWNneldqUEh3WEpxOUdUNS5WejZNa3RnaHRzaDlHUHd3SjFIZThMN2JvWkpWZ0VYczRkdlZQUHBhZkpnR25Td2tnLlNleUowSWpvaVpHMGlMQ0p6SWpvaWFIUjBjSE02THk5ck9ITXRaR1YyTG1GMFlXeGhjSEpwYzIwdWFXOHZjSEpwYzIwdFlXZGxiblF2Wkdsa1kyOXRiU0lzSW5JaU9sdGRMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDAiLCJib2R5Ijp7ImdvYWxfY29kZSI6ImlvLmF0YWxhcHJpc20uY29ubmVjdCIsImdvYWwiOiJFc3RhYmxpc2ggYSB0cnVzdCBjb25uZWN0aW9uIGJldHdlZW4gdHdvIHBlZXJzIHVzaW5nIHRoZSBwcm90b2NvbCAnaHR0cHM6Ly9hdGFsYXByaXNtLmlvL21lcmN1cnkvY29ubmVjdGlvbnMvMS4wL3JlcXVlc3QnIiwiYWNjZXB0IjpbXX19")
        setupStreamObservers()
//        binding.startAgent.setOnClickListener {
//            context?.let { it1 -> viewModel.startAgent(it1) }
// findNavController().navigate(R.id.action_First2Fragment_to_SecondFragment)
//        }
//        binding.sendMessage.setOnClickListener {
// viewModel.sendTestMessage()
//            viewModel.createPeerDid()
//        }
//        binding.createPeerDID.setOnClickListener {
//            viewModel.createPeerDid()
//        }
//        binding.parseInviteBtn.setOnClickListener {
//            binding.oobField.editText?.text?.let {
//                if (it.toString().isBlank()) {
//                    Snackbar.make(binding.root, resources.getString(R.string.need_oob), Snackbar.LENGTH_LONG)
//                        .show()
//                } else if (isValidURL(it.toString())) {
//                    try {
//                        viewModel.parseAndAcceptOOB(it.toString())
//                    } catch (ex: Exception) {
//                        Snackbar.make(binding.root, ex.toString(), Snackbar.LENGTH_LONG)
//                            .show()
//                    }
//                } else {
//                    Snackbar.make(binding.root, resources.getString(R.string.not_valid_oob), Snackbar.LENGTH_LONG)
//                        .show()
//                }
//            } ?: run {
//                Snackbar.make(binding.root, resources.getString(R.string.need_oob), Snackbar.LENGTH_LONG)
//                    .show()
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        viewModel.stopAgent()
        _binding = null
    }

    @Throws(MalformedURLException::class, URISyntaxException::class)
    private fun isValidURL(url: String): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: MalformedURLException) {
            false
        } catch (e: URISyntaxException) {
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupStreamObservers() {
//        viewModel.messageListStream().observe(this.viewLifecycleOwner) { messages ->
//            messages.forEach { message ->
//                var textToAppend = "${message.id}: ${message.body} \n"
//                message.attachments.forEach { attachment ->
//                    textToAppend += "Attachment ID: ${attachment.id}: \n"
//                }
//                logger.info(textToAppend)
//            }
//        }
//        viewModel.notificationListStream().observe(this.viewLifecycleOwner) {
//            binding.log.append(it)
//        }
//        viewModel.agentStateStream().observe(this.viewLifecycleOwner) {
//            binding.log.append("Agent state: $it \n")
//        }
    }
}
