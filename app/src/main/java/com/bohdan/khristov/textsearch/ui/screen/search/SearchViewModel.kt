package com.bohdan.khristov.textsearch.ui.screen.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.domain.common.Resource.Status.*
import com.bohdan.khristov.textsearch.util.default
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val searchInteractor: SearchInteractor) :
    ViewModel() {

    val searchModel = MutableLiveData<SearchModel>()

    val totalEntries = MutableLiveData<Int>().default(0)
    val progress = MutableLiveData<Int>().default(0)
    val searchStatus = MutableLiveData<SearchStatus>()

    fun search(searchRequest: SearchRequest) {
        searchStatus.value = SearchStatus.IN_PROGRESS
        searchInteractor.fullSearch(searchRequest) {
            when (it.status) {
                SUCCESS -> {
                    it.data?.let { searchModel ->
                        val totalEntries = totalEntries.value?.plus(searchModel.result.entriesCount)
                        this.totalEntries.postValue(totalEntries)

                        progress.postValue(progress.value?.plus(1))

                        this.searchModel.postValue(searchModel)
                    }
                }
                ERROR -> {
                }
                LOADING -> {
                }
            }
        }
    }

    fun stopSearch() {
        searchInteractor.stopSearch()
    }
}