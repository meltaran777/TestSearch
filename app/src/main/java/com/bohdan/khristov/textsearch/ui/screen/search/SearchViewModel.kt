package com.bohdan.khristov.textsearch.ui.screen.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.domain.common.Resource.Status.*
import com.bohdan.khristov.textsearch.util.L
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val searchInteractor: SearchInteractor) :
    ViewModel() {

    val searchModel = MutableLiveData<SearchModel>()
    val totalTextEntries = MutableLiveData<Int>()

    fun search(searchRequest: SearchRequest) {
        searchInteractor.search(searchRequest) {
            when (it.status) {
                SUCCESS -> {
                    it.data?.let { searchModel ->
                        val currentCount = totalTextEntries.value ?: 0
                        val totalCount = currentCount + searchModel.result.entriesCount

                        L.log("SearchViewModel", "currentCount = $currentCount")
                        L.log("SearchViewModel", "totalCount = $totalCount")

                        totalTextEntries.postValue(totalCount)
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
}