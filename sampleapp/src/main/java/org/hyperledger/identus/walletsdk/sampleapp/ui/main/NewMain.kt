package org.hyperledger.identus.walletsdk.sampleapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hyperledger.identus.walletsdk.sampleapp.databinding.NewMainBinding
import org.hyperledger.identus.walletsdk.sampleapp.ui.issuance.IssuanceScreenFragment

class NewMain: AppCompatActivity() {

    private lateinit var binding: NewMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}