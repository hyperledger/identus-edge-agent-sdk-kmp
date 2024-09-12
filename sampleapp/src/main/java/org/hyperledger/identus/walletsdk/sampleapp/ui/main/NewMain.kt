package org.hyperledger.identus.walletsdk.sampleapp.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hyperledger.identus.walletsdk.sampleapp.R
import org.hyperledger.identus.walletsdk.sampleapp.databinding.NewMainBinding
import org.hyperledger.identus.walletsdk.sampleapp.ui.issuance.IssuanceScreenFragment

class NewMain : AppCompatActivity() {

    private lateinit var binding: NewMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleNewIntent(it)
        }
    }

    private fun handleNewIntent(intent: Intent) {
        val data = intent.data
        // Pass the data to the fragment
        if (data != null) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_view) as? IssuanceScreenFragment
            fragment?.handleCallbackUrl(data)
        }
    }
}
