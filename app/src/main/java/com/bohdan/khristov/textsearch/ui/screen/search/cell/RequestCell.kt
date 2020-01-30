package com.bohdan.khristov.textsearch.ui.screen.search.cell

import android.view.View
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import io.techery.celladapter.Cell
import io.techery.celladapter.Layout
import kotlinx.android.synthetic.main.item_request.view.*

@Layout(R.layout.item_request)
class RequestCell(val view: View) : Cell<SearchRequest, RequestCell.Listener>(view) {

    override fun bindView() {
        item?.let { searchModel ->
            view.urlTv.text = searchModel.url
        }
    }

    interface Listener : Cell.Listener<SearchRequest>
}