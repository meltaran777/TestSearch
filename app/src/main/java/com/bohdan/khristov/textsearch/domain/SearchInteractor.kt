package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchResult
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.countEntries
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

const val URL_PROCESS_TIMEOUT = 10_000L

class SearchInteractor @Inject constructor(private val searchRepository: ISearchRepository) :
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val job = Job()

    private var state: SearchState? = null

    @ObsoleteCoroutinesApi
    fun search(searchRequest: SearchRequest) {
        state = SearchState(this, rootRequest = searchRequest)
        launch { wideSearch(searchRequest) }
    }

    private suspend fun wideSearch(request: SearchRequest) {
        try {
            val channel: Channel<SearchRequest> = Channel()
            launch {
                val childUrls = state!!.getCurrentLevelUrls()
                childUrls.forEachIndexed { index, childUrl ->
                    if (state!!.isSendRequestEnable()) {
                        val childRequest = request.copy(url = childUrl)
                        channel.send(childRequest)
                        state!!.incRequest()
                    }
                }
                channel.close()
            }

            val jobs = mutableListOf<Job>()
            for (i in 0..request.threadCount) {
                jobs.add(launch {
                    for (childRequest in channel) {
                        state!!.addProcessingRequest(childRequest)
                        val childResult = singleSearch(childRequest)
                        if (childResult != null) {
                            val childModel = SearchModel(childRequest, childResult)
                            state!!.addSearchResult(childModel)
                        } else {
                            state!!.decRequest()
                        }
                    }
                })
            }
            jobs.forEach { it.join() }

            val firstUrlOnNextLevel = state!!.getNextLevelUrls().firstOrNull()
            when (firstUrlOnNextLevel != null) {
                true -> {
                    state!!.incLevel()
                    val searchRequest = request.copy(url = firstUrlOnNextLevel)
                    wideSearch(searchRequest)
                }
                false -> {
                    state!!.changeStatus(SearchStatus.COMPLETED)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun singleSearch(searchRequest: SearchRequest): SearchResult? {
        return withTimeoutOrNull(URL_PROCESS_TIMEOUT) {
            val fetchedText = searchRepository.getText(searchRequest.url)
            val entriesCount = async { fetchedText.countEntries(searchRequest.textToFind) }
            val parentUrls = async { fetchedText.extractUrls() }
            val searchResult = SearchResult(entriesCount.await(), parentUrls.await())
            searchResult
        }
    }

    fun receiveRequest(): ReceiveChannel<SearchRequest> = state!!.processingRequestChannel

    fun receiveResult(): ReceiveChannel<SearchModel> = state!!.processedRequestChannel

    fun receiveStatus(): ReceiveChannel<SearchStatus> = state!!.statusChannel

    fun stop() {
        coroutineContext.cancelChildren()
    }
}