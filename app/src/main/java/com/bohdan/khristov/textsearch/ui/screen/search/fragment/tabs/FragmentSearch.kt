package com.bohdan.khristov.textsearch.ui.screen.search.fragment.tabs

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchStatus.*
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import com.bohdan.khristov.textsearch.ui.dialogs.CreateSearchDialog
import com.bohdan.khristov.textsearch.ui.dialogs.IDialog
import com.bohdan.khristov.textsearch.ui.screen.search.SearchViewModel
import com.bohdan.khristov.textsearch.util.toggleVisibility
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class FragmentSearch : BaseFragment() {

    private var createSearchDialog: IDialog? = null

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
                PREPARE -> view.context.getString(R.string.prepare)
            }
            statusTv.text = text

            emptySearchLabelTv.toggleVisibility(status == PREPARE)
            searchCardView.toggleVisibility(status != PREPARE)

            currentUrlPb.toggleVisibility(status == IN_PROGRESS)
        })

        createSearchDialog = CreateSearchDialog(view.context) { request ->
            searchPb.max = request.maxUrlCount
            rootUrlTv.text = request.url
            searchForTv.text = request.textToFind
            searchViewModel.search(request)
        }

        fab.setOnClickListener {
            createSearchDialog?.show()
        }
        stopBtn.setOnClickListener {
            searchViewModel.stopSearch()
        }
    }

    override fun getLayoutId() = R.layout.fragment_search

    companion object {
        fun newInstance() = FragmentSearch()
    }
}