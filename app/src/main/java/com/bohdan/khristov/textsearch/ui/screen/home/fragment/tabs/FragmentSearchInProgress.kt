package com.bohdan.khristov.textsearch.ui.screen.home.fragment.tabs

import android.os.Bundle
import android.view.View
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.ui.common.BaseFragment
import com.bohdan.khristov.textsearch.ui.dialogs.CreateSearchDialog
import com.bohdan.khristov.textsearch.ui.dialogs.IDialog
import io.techery.celladapter.CellAdapter
import kotlinx.android.synthetic.main.fragment_search_in_progress.*

class FragmentSearchInProgress : BaseFragment() {

    private lateinit var createSearchDialog: IDialog

    private var adapter = CellAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createSearchDialog = CreateSearchDialog(view.context)

        fab.setOnClickListener {
            createSearchDialog.show()
        }
    }

    override fun getLayoutId() = R.layout.fragment_search_in_progress

    companion object {
        fun newInstance() = FragmentSearchInProgress()
    }
}