package org.hyperledger.identus.walletsdk.sampleapp.ui.issuance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.hyperledger.identus.walletsdk.sampleapp.databinding.FragmentIssuanceBinding
import org.hyperledger.identus.walletsdk.sampleapp.ui.messages.MessagesViewModel

class IssuanceScreenFragment : Fragment() {

    private var _binding: FragmentIssuanceBinding? = null
    private val viewModel: IssuanceViewModel by viewModels()

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentIssuanceBinding.inflate(inflater, container, false)
        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            val url = state.authorizationRequestPrepared.authorizationCodeURL.value

            binding.webview
                .loadUrl(url.toString())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signIn.setOnClickListener {
            viewModel.signIn()
        }
    }
}
