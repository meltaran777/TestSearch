package com.bohdan.khristov.textsearch.ui.screen.search.cell

import androidx.recyclerview.widget.DiffUtil
import com.bohdan.khristov.textsearch.data.model.SearchRequest

class SearchRequestDiffCallback(
    private val newItems: List<SearchRequest>,
    private val oldItems: List<SearchRequest>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}