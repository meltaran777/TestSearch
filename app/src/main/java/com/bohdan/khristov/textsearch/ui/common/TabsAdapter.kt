package com.bohdan.khristov.textsearch.ui.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()

    override fun getItem(position: Int) = fragments.get(position)

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int): CharSequence? {
        return if (titles.size == fragments.size) titles[position] else ""
    }

    fun addFragment(f: Fragment, title: String) {
        fragments.add(f)
        titles.add(title)
    }

    fun addFragment(f: Fragment) {
        fragments.add(f)
    }

    fun clear() {
        fragments.clear()
    }
}