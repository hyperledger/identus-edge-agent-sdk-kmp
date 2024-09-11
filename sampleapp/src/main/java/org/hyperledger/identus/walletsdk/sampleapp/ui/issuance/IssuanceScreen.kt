package org.hyperledger.identus.walletsdk.sampleapp.ui.issuance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

//            binding.webview
//                .loadUrl(url.toString())
        }
//      http://192.168.68.113:9981/realms/oid4vci-holder/protocol/openid-connect/auth?resource=http%3A%2F%2F192.168.68.113%3A8090%2Foid4vci%2Fissuers%2F13ac8bde-9f01-4570-b858-646c3cb243d1&scope=StudentProfile&response_type=code&issuer_state=d32e575b-d482-448d-a773-a0e9a7fde193&redirect_uri=edgeagentsdk%3A%2F%2Foidc.login&state=KHL2vEJhffqPKg1ctgHGMRxWhQQ-1oIyomjubHHKvUA&code_challenge_method=S256&prompt=login&client_id=alice-wallet&code_challenge=eifXZ7AioJDXK_cS-F_bulz92Qk-z_6XAGxasx0oqkg
//      http://localhost:7777/?state=ZC-3KCC8bvuwGJJAccdR_HWJ5MngBtXtjWvYxlQ66cU&session_state=d941330c-50ec-46ff-ba5a-f9ec66304641&iss=http%3A%2F%2F192.168.68.113%3A9981%2Frealms%2Foid4vci-holder&code=47183d07-63b7-43c6-9b08-ad90f7667465.d941330c-50ec-46ff-ba5a-f9ec66304641.alice-wallet
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signIn.setOnClickListener {
            viewModel.signIn()
        }
    }
}
