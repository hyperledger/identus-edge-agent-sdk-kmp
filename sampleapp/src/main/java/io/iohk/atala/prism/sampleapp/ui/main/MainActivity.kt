package io.iohk.atala.prism.sampleapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import io.iohk.atala.prism.sampleapp.Sdk
import io.iohk.atala.prism.sampleapp.databinding.ActivityMainBinding
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
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
        viewModel.agentStatusStream().observe(this) {
            if (it == PrismAgent.State.RUNNING.name) {
//                binding.agentStatus.text = String.format(getString(R.string.agent_status), it)
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
        Sdk.getInstance(this).stopAgent()
    }
}
