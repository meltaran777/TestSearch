package com.bohdan.khristov.textsearch.ui.screen.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.util.default
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val searchInteractor: SearchInteractor) :
    ViewModel() {

    val searchModel = MutableLiveData<SearchModel>()
    val processingUrl = MutableLiveData<String>()
    val totalEntries = MutableLiveData<Int>().default(0)
    val progress = MutableLiveData<Int>().default(0)
    val searchStatus = MutableLiveData<SearchStatus>().default(SearchStatus.PREPARE)

    private val jobs = mutableListOf<Job>()

    @ObsoleteCoroutinesApi
    fun search(searchRequest: SearchRequest) {
        if (searchStatus.value == SearchStatus.IN_PROGRESS) {
            stopSearch()
        }
        cleanOldResult()
        searchStatus.value = SearchStatus.IN_PROGRESS
        jobs.add(searchInteractor.fullSearch(searchRequest,
            onStartProcessingUrl = { request ->
                processingUrl.postValue(request.url)
            },
            onUrlProcessed = { searchModel ->
                val totalEntries = totalEntries.value?.plus(searchModel.result.entriesCount)
                this.totalEntries.postValue(totalEntries)

                progress.postValue(progress.value?.plus(1))

                this.searchModel.postValue(searchModel)
            }, onCompleted = {
                searchStatus.postValue(SearchStatus.COMPLETED)
            })
        )
    }

    fun stopSearch() {
        cancelJobs()
        searchStatus.value = SearchStatus.COMPLETED
    }

    private fun cleanOldResult() {
        progress.value = 0
        totalEntries.value = 0
        processingUrl.value = ""
        searchModel.value = SearchModel.empty()
    }

    private fun cancelJobs() {
        jobs.forEach { it.cancel() }
    }

    override fun onCleared() {
        cancelJobs()
        super.onCleared()
    }
}