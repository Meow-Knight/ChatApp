package com.lecaoviethuy.messengerapp.adapterClasses

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ViewPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm) {
    private var fragmets : ArrayList<Fragment> = ArrayList()

    public fun addFragment(fragment : Fragment){
        fragmets.add(fragment)
    }

    override fun getCount(): Int {
        return fragmets.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmets[position]
    }
}