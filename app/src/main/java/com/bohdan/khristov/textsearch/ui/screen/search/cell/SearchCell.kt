package com.bohdan.khristov.textsearch.ui.screen.search.cell

import android.view.View
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchModel
import io.techery.celladapter.Cell
import io.techery.celladapter.Layout
import kotlinx.android.synthetic.main.item_search.view.*

@Layout(R.layout.item_search)
class SearchCell(val view: View) : Cell<SearchModel, SearchCell.Listener>(view) {

    override fun bindView() {
        item?.let { searchModel ->
            view.rootUrlTv.text = searchModel.request.rootUrl
            view.findForTv.text = searchModel.request.textToFind
            view.entriesCountTv.text = searchModel.result.textEntries.toString()
        }
    }

    interface Listener : Cell.Listener<SearchModel>
}