package com.bohdan.khristov.textsearch.ui.screen.search.fragment.tabs

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.data.model.SearchStatus.*
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import com.bohdan.khristov.textsearch.ui.screen.search.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.entriesCountTv
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class FragmentSearch : BaseFragment() {

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel = ViewModelProviders.of(activity!!).get(SearchViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel.searchModel.observe(this, Observer { searchModel ->
            currentUrlTv.text = searchModel.request.url
        })
        searchViewModel.totalEntries.observe(this, Observer { totalCount ->
            entriesCountTv.text = totalCount.toString()
        })
        searchViewModel.progress.observe(this, Observer { progress ->
            searchPb.progress = progress
        })
        searchViewModel.searchStatus.observe(this, Observer { status ->
            val text = when (status) {
                IN_PROGRESS -> getString(R.string.in_progress)
                COMPLETED -> getString(R.string.completed)
            }
            statusTv.text = text
        })

        searchBtn.setOnClickListener {
            val searchRequest = SearchRequest(
                url = urlEt.text.toString(),
                textToFind = textToFindEt.text.toString(),
                maxUrlCount = maxUrlCountEt.text.toString().toInt(),
                threadCount = threadCountEt.text.toString().toInt()
            )
            searchPb.max = searchRequest.maxUrlCount
            searchViewModel.search(searchRequest)
        }
    }

    override fun getLayoutId() = R.layout.fragment_search

    companion object {
        fun newInstance() = FragmentSearch()
    }
}