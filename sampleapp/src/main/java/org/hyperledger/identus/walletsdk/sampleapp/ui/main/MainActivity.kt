package org.hyperledger.identus.walletsdk.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import org.hyperledger.identus.walletsdk.databinding.ActivityMainBinding
import org.hyperledger.identus.walletsdk.prismagent.PrismAgent
import org.hyperledger.identus.walletsdk.sampleapp.Sdk

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewPager = binding.viewPager
        tabs = binding.tabs
        Sdk.getInstance().agentStatusStream().observe(this) {
            if (it == PrismAgent.State.RUNNING) {
                Snackbar.make(binding.root, "Agent state: $it", Snackbar.LENGTH_LONG).show()
                agentStartedShowViews()
            }
        }
    }

    private fun agentStartedShowViews() {
        viewPager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(viewPager)
        binding.agentView.visibility = View.GONE
        binding.viewPager.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        Sdk.getInstance().stopAgent()
    }
}
