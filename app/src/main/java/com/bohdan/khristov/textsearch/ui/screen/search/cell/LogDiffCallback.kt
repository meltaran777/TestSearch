package com.bohdan.khristov.textsearch.ui.screen.search.cell

import androidx.recyclerview.widget.DiffUtil
import com.bohdan.khristov.textsearch.data.model.SearchModel


class LogDiffCallback(
    private val newItems: List<SearchModel>,
    private val oldItems: List<SearchModel>
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