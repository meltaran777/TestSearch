package com.bohdan.khristov.textsearch.ui.screen.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.util.default
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SearchViewModel @Inject constructor(private val searchInteractor: SearchInteractor) :
    ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val processingUrl = MutableLiveData<String>()
    val processedRequests = MutableLiveData<List<SearchModel>>()
    val entriesCount = MutableLiveData<Int>().default(0)
    val progress = MutableLiveData<Int>().default(0)
    val searchStatus = MutableLiveData<SearchStatus>().default(SearchStatus.PREPARE)

    private val job = Job()

    fun search(searchRequest: SearchRequest) {
        clean()
        searchInteractor.search(searchRequest)
        launch {
            for (requests in searchInteractor.receiveInProgressRequests()) {
                processingUrl.postValue(requests.lastOrNull()?.url ?: "")
            }
        }
        launch {
            for (info in searchInteractor.receiveInfo()) {
                entriesCount.postValue(info.entriesCount)
                progress.postValue(info.progress)
                processedRequests.postValue(info.processedRequests)
            }
        }
        launch {
            for (status in searchInteractor.receiveStatus()) {
                searchStatus.postValue(status)
            }
        }
    }

    private fun clean() {
        entriesCount.value = 0
        progress.value = 0
        processingUrl.value = ""
    }

    fun stop() {
        launch {
            searchInteractor.stop()
        }
    }

    override fun onCleared() {
        searchInteractor.coroutineContext.cancelChildren()
        coroutineContext.cancelChildren()
        super.onCleared()
    }
}