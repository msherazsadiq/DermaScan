// IntroSliderAdapter.kt
package com.sherazsadiq.dermascan.introslider

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstSliderFragment()
            1 -> SecondSliderFragment()
            2 -> ThirdSliderFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}