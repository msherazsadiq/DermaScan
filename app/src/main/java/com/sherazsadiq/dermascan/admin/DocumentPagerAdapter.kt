package com.sherazsadiq.dermascan.admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DocumentPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val documentTypes = listOf("DegreeCertificate", "GovernmentID", "MedicalLicense")

    override fun getItemCount(): Int = documentTypes.size

    override fun createFragment(position: Int): Fragment {
        return DocumentFragment.newInstance(documentTypes[position])
    }
}