package com.bohdan.khristov.textsearch.ui.screen.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.data.model.SearchInfo
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.util.default
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val MAX_UPDATE_DELAY = 3_000L

class SearchViewModel @Inject constructor(private val searchInteractor: SearchInteractor) :
    ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    val requestsInProgress = MutableLiveData<List<SearchRequest>>()
    val processedRequests = MutableLiveData<List<SearchModel>>()
    val entriesCount = MutableLiveData<Int>().default(0)
    val progress = MutableLiveData<Int>().default(0)
    val searchStatus = MutableLiveData<SearchStatus>().default(SearchStatus.PREPARE)

    private var updateChannel: ReceiveChannel<Unit>? = null

    private var lastRequests = listOf<SearchRequest>()
    private var lastResults = listOf<SearchModel>()

    private var job: Job = Job()
    private var jobs = mutableListOf<Job>()

    //TODO: handle case when search already running when start new one
    fun search(request: SearchRequest) {
        launch {
            withContext(Dispatchers.Main + job) { clean() }
            jobs.add(searchInteractor.search(request, object : SearchInteractor.Listener {
                override fun onStatusChanged(status: SearchStatus) {
                    searchStatus.postValue(status)
                }

                override fun onInfoChanged(info: SearchInfo) {
                    entriesCount.postValue(info.entriesCount)
                    progress.postValue(info.progress)
                    lastResults = info.processedRequests
                    lastRequests = info.inProgressRequests
                }
            }))
            launch {
                updateChannel =
                    ticker(
                        delayMillis = calculateListUpdateDelay(request),
                        initialDelayMillis = 0
                    )
                updateChannel?.let { channel ->
                    for (event in channel) {
                        requestsInProgress.postValue(lastRequests.toMutableList())
                        processedRequests.postValue(lastResults.toMutableList())
                    }
                }
            }
        }
    }

    private fun calculateListUpdateDelay(request: SearchRequest): Long {
        val delay = request.maxParallelUrl * 100L
        return if (delay > MAX_UPDATE_DELAY) MAX_UPDATE_DELAY else delay
    }

    private fun clean() {
        updateChannel?.cancel()
        entriesCount.value = 0
        progress.value = 0
        lastResults = mutableListOf()
        lastRequests = mutableListOf()
        requestsInProgress.value = mutableListOf()
    }

    fun stop() {
        searchInteractor.stop()
        updateChannel?.cancel()
    }

    override fun onCleared() {
        jobs.forEach { it.cancel() }
        updateChannel?.cancel()
        super.onCleared()
    }
}