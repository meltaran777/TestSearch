package com.bohdan.khristov.textsearch.ui.screen.search.fragment.tabs

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import com.bohdan.khristov.textsearch.ui.dialogs.CreateSearchDialog
import com.bohdan.khristov.textsearch.ui.dialogs.IDialog
import com.bohdan.khristov.textsearch.ui.screen.search.SearchViewModel
import com.bohdan.khristov.textsearch.ui.screen.search.cell.SearchCell
import com.bohdan.khristov.textsearch.util.L
import io.techery.celladapter.CellAdapter
import kotlinx.android.synthetic.main.fragment_search_in_progress.*

class FragmentSearchInProgress : BaseFragment() {

    private lateinit var searchViewModel: SearchViewModel

    private lateinit var createSearchDialog: IDialog

    private var adapter = CellAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel = ViewModelProviders.of(activity!!).get(SearchViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.registerCell(SearchModel::class.java, SearchCell::class.java)

        searchInProgressRv.layoutManager = LinearLayoutManager(view.context)
        searchInProgressRv.adapter = adapter

        searchViewModel.searchModel.observe(activity!!, Observer { searchModel ->
            L.log("FragmentSearchInProgress","searchModel = $searchModel")
            adapter.addItem(searchModel)
        })

        createSearchDialog = CreateSearchDialog(view.context) { searchRequest ->
            searchViewModel.search(searchRequest)
        }

        fab.setOnClickListener {
            createSearchDialog.show()
        }
    }

    override fun getLayoutId() = R.layout.fragment_search_in_progress

    companion object {
        fun newInstance() = FragmentSearchInProgress()
    }
}