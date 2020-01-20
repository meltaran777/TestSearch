package com.bohdan.khristov.textsearch.ui.screen.home.fragment

import android.os.Bundle
import android.view.View
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import com.bohdan.khristov.textsearch.ui.common.TabsAdapter
import com.bohdan.khristov.textsearch.ui.screen.home.fragment.tabs.FragmentSearchHistory
import com.bohdan.khristov.textsearch.ui.screen.home.fragment.tabs.FragmentSearchInProgress
import kotlinx.android.synthetic.main.fragment_home_tabs.*

class TabsFragment : BaseFragment() {

    private lateinit var adapter: TabsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(getString(R.string.home))

        adapter = TabsAdapter(childFragmentManager)
        adapter.addFragment(FragmentSearchInProgress.newInstance(), getString(R.string.in_progress))
        adapter.addFragment(FragmentSearchHistory.newInstance(), getString(R.string.history))

        viewPager.offscreenPageLimit = OFF_SCREEN_PAGE_LIMIT
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroyView() {
        adapter.clear()

        super.onDestroyView()
    }

    override fun getLayoutId() = R.layout.fragment_home_tabs

    companion object {

        const val OFF_SCREEN_PAGE_LIMIT = 1

        fun newInstance() = TabsFragment()
    }
}