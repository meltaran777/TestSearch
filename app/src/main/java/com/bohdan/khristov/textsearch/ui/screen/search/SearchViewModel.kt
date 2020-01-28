package com.bohdan.khristov.textsearch.ui.screen.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.util.default
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val searchInteractor: SearchInteractor) :
    ViewModel() {

    val processingUrl = MutableLiveData<String>()
    val searchModels = MutableLiveData<MutableList<SearchModel>>()
    val totalEntries = MutableLiveData<Int>().default(0)
    val progress = MutableLiveData<Int>().default(0)
    val searchStatus = MutableLiveData<SearchStatus>().default(SearchStatus.PREPARE)

    private val totalEntriesAtomic = AtomicInteger(0)
    private val progressAtomic = AtomicInteger(0)
    private var models = Collections.synchronizedList(mutableListOf<SearchModel>())

    @ObsoleteCoroutinesApi
    fun search(searchRequest: SearchRequest) {
        if (searchStatus.value == SearchStatus.IN_PROGRESS) {
            stopSearch()
        }
        cleanOldResult()
        searchStatus.value = SearchStatus.IN_PROGRESS
        searchInteractor.search1(searchRequest,
            onStartProcessingUrl = { request ->
                processingUrl.postValue(request.url)
            },
            onUrlProcessed = { searchModel ->
                totalEntriesAtomic.addAndGet(searchModel.result.entriesCount)
                this@SearchViewModel.totalEntries.postValue(totalEntriesAtomic.toInt())

                progressAtomic.incrementAndGet()
                progress.postValue(progressAtomic.toInt())

                models.add(searchModel)
                this@SearchViewModel.searchModels.postValue(models)
            }, onCompleted = {
                searchStatus.postValue(SearchStatus.COMPLETED)
            })
    }

    fun stopSearch() {
        searchInteractor.stop()
        searchStatus.value = SearchStatus.COMPLETED
    }

    private fun cleanOldResult() {
        progressAtomic.set(0)
        totalEntriesAtomic.set(0)
        progress.value = 0
        totalEntries.value = 0
        processingUrl.value = ""
        models = mutableListOf()
        searchModels.value = mutableListOf()
    }

    override fun onCleared() {
        stopSearch()
        super.onCleared()
    }
}