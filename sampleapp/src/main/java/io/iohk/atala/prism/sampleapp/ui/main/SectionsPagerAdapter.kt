package io.iohk.atala.prism.sampleapp.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.iohk.atala.prism.sampleapp.R
import io.iohk.atala.prism.sampleapp.ui.agent.AgentFragment
import io.iohk.atala.prism.sampleapp.ui.connections.ConnectionsFragment
import io.iohk.atala.prism.sampleapp.ui.credentials.CredentialsFragment
import io.iohk.atala.prism.sampleapp.ui.messages.MessagesFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_agent,
    R.string.tab_connections,
    R.string.tab_messages,
    R.string.tab_credentials
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                AgentFragment.newInstance()
            }
            1 -> {
                ConnectionsFragment.newInstance()
            }
            2 -> {
                MessagesFragment.newInstance()
            }
            3 -> {
                CredentialsFragment.newInstance()
            }
            else -> {
                throw Exception("No fragment in that position")
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}
