package com.bohdan.khristov.textsearch.ui.screen.search.fragment.tabs

import android.os.Bundle
import android.view.View
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import io.techery.celladapter.CellAdapter

class FragmentSearchHistory : BaseFragment() {

    private var adapter = CellAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId() = R.layout.fragment_search_history

    companion object {
        fun newInstance() = FragmentSearchHistory()
    }
}