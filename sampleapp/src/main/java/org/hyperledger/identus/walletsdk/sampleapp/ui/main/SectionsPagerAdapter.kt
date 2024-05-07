package org.hyperledger.identus.walletsdk.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.hyperledger.identus.walletsdk.R
import org.hyperledger.identus.walletsdk.ui.contacts.ContactsFragment
import org.hyperledger.identus.walletsdk.ui.credentials.CredentialsFragment
import org.hyperledger.identus.walletsdk.ui.dids.DIDsFragment
import org.hyperledger.identus.walletsdk.ui.messages.MessagesFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_contacts,
    R.string.tab_dids,
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
                ContactsFragment.newInstance()
            }
            1 -> {
                DIDsFragment.newInstance()
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
