package org.hyperledger.identus.walletsdk.sampleapp.ui.issuance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import java.net.URI
import org.hyperledger.identus.walletsdk.sampleapp.databinding.FragmentIssuanceBinding

class IssuanceScreenFragment : Fragment() {

    private var _binding: FragmentIssuanceBinding? = null
    private val viewModel: IssuanceViewModel by viewModels()

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentIssuanceBinding.inflate(inflater, container, false)
        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            val url = state.authorizationRequestPrepared.authorizationCodeURL
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
            startActivity(browserIntent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signIn.setOnClickListener {
            viewModel.signIn()
        }
    }

    fun handleCallbackUrl(uri: Uri) {
        viewModel.handleCallbackUrl(URI(uri.toString()))
    }
}
