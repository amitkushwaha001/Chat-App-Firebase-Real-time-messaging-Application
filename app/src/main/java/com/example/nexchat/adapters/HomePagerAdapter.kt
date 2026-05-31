package com.example.nexchat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.nexchat.fragments.*

class HomePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatsFragment()
            1 -> ContactsFragment()
            2 -> StatusFragment() // Using Status as a placeholder for Settings/other
            3 -> CallsFragment() // Using Calls as a placeholder for Profile
            else -> ChatsFragment()
        }
    }
}
