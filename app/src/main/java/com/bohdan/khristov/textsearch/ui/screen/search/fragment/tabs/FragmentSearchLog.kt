package com.bohdan.khristov.textsearch.ui.screen.search.fragment.tabs

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import com.bohdan.khristov.textsearch.ui.screen.search.SearchViewModel
import com.bohdan.khristov.textsearch.ui.screen.search.cell.SearchCell
import io.techery.celladapter.CellAdapter
import kotlinx.android.synthetic.main.fragment_search_history.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DiffUtil
import com.bohdan.khristov.textsearch.ui.screen.search.cell.SearchModelDiffCallback

class FragmentSearchLog : BaseFragment() {

    private var adapter = CellAdapter()

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel = ViewModelProviders.of(activity!!).get(SearchViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.registerCell(SearchModel::class.java, SearchCell::class.java)

        val linearLayoutManager = LinearLayoutManager(view.context)
        searchLogRv.layoutManager = linearLayoutManager
        searchLogRv.adapter = adapter
        val dividerItemDecoration =
            DividerItemDecoration(searchLogRv.context, linearLayoutManager.orientation)
        searchLogRv.addItemDecoration(dividerItemDecoration)

        searchViewModel.processedRequests.observe(this, Observer { items ->
            searchLogRv.postDelayed({
                val diffResult = DiffUtil.calculateDiff(
                    SearchModelDiffCallback(
                        newItems = items,
                        oldItems = adapter.items.filterIsInstance<SearchModel>().toList()
                    )
                )
                adapter.items.clear()
                adapter.items.addAll(items)
                diffResult.dispatchUpdatesTo(adapter)
            }, LOG_UPDATE_DELAY)
        })
    }

    override fun getLayoutId() = R.layout.fragment_search_history

    companion object {
        const val LOG_UPDATE_DELAY = 1000L

        fun newInstance() = FragmentSearchLog()
    }
}