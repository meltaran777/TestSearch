package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.*
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.countEntries
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

const val REQUEST_PROCESS_TIMEOUT = 10_000L

class SearchInteractor @Inject constructor(private val searchRepository: ISearchRepository) :
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val job = Job()

    private var state: SearchState? = null

    @ObsoleteCoroutinesApi
    fun search(searchRequest: SearchRequest) {
        if (state?.status == SearchStatus.IN_PROGRESS) {
            close()
        }
        launch {
            state = SearchState(rootRequest = searchRequest)
            state!!.changeStatus(SearchStatus.IN_PROGRESS)
            wideSearch(searchRequest)
        }
    }

    private suspend fun wideSearch(rootRequest: SearchRequest) {
        try {
            val requestChannel = produce {
                for (request in state!!.getCurrentLevelRequests()) {
                    if (state!!.isSendRequestEnable()) {
                        send(request)
                        state!!.incRequestCounter()
                    }
                }
            }

            val jobs = mutableListOf<Job>()
            for (i in 0..rootRequest.threadCount) {
                jobs.add(launch {
                    for (request in requestChannel) {
                        state!!.addInProgressRequest(request)
                        val result = singleSearch(request) ?: SearchResult.empty()
                        state!!.addSearchResult(SearchModel(request, result))
                    }
                })
            }
            jobs.forEach { it.join() }

            val firstRequestOnNextLevel = state!!.getNextLevelRequests().firstOrNull()
            when (firstRequestOnNextLevel != null) {
                true -> {
                    state!!.incLevel()
                    wideSearch(firstRequestOnNextLevel)
                }
                false -> {
                    state!!.changeStatus(SearchStatus.COMPLETED)
                    state!!.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun singleSearch(searchRequest: SearchRequest): SearchResult? {
        return withTimeoutOrNull(REQUEST_PROCESS_TIMEOUT) {
            val fetchedText = searchRepository.getText(searchRequest.url)
            val entriesCount = async { fetchedText.countEntries(searchRequest.textToFind) }
            val parentUrls = async { fetchedText.extractUrls() }
            val searchResult = SearchResult(entriesCount.await(), parentUrls.await())
            searchResult
        }
    }

    fun receiveInProgressRequests(): ReceiveChannel<List<SearchRequest>> =
        state!!.inProgressRequestsChannel

    fun receiveInfo(): ReceiveChannel<SearchInfo> = state!!.infoChannel

    fun receiveStatus(): ReceiveChannel<SearchStatus> = state!!.statusChannel

    fun stop() {
        launch {
            state?.changeStatus(SearchStatus.COMPLETED)
            close()
        }
    }

    fun close() {
        state?.close()
        coroutineContext.cancelChildren()
    }
}