package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.*
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.countEntries
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

const val URL_PROCESS_TIMEOUT = 10_000L

class SearchInteractor @Inject constructor(
    private val searchRepository: ISearchRepository
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val job = Job()
    private val mutex = Mutex()

    private var processingRequestChannel = Channel<SearchRequest>()
    private var processedRequestChannel = Channel<SearchModel>()
    private var statusChannel = Channel<SearchStatus>()

    private lateinit var cache: SearchCache

    @ObsoleteCoroutinesApi
    fun search1(searchRequest: SearchRequest) {
        processingRequestChannel = Channel()
        processedRequestChannel = Channel()
        statusChannel = Channel()

        cache = SearchCache(searchRequest)

        launch { search(searchRequest) }
    }

    private suspend fun search(rootRequest: SearchRequest) {
        try {
            val channel: Channel<SearchRequest> = Channel()
            launch {
                val childUrls = cache.getCurrentLevelUrls()
                childUrls.forEachIndexed { index, childUrl ->
                    if (cache.isSendRequestEnable()) {
                        val childRequest = rootRequest.copy(url = childUrl)
                        channel.send(childRequest)
                    }
                    cache.incRequest()
                }
                channel.close()
            }

            val jobs = mutableListOf<Job>()
            for (i in 0..rootRequest.threadCount) {
                jobs.add(launch {
                    for (searchRequest in channel) {
                        launch {
                            if (!processingRequestChannel.isClosedForSend)
                                processingRequestChannel.send(searchRequest)
                        }
                        val searchResult = singleSearch(searchRequest)
                        val searchModel = SearchModel(searchRequest, searchResult)
                        launch {
                            if (!processedRequestChannel.isClosedForSend)
                                processedRequestChannel.send(searchModel)
                        }
                    }
                })
            }
            jobs.forEach { it.join() }

            val firstUrlOnNextLevel = mutex.withLock { cache.getNextLevelUrls().firstOrNull() }
            when (firstUrlOnNextLevel != null) {
                true -> {
                    cache.incLevel()
                    val searchRequest = rootRequest.copy(url = firstUrlOnNextLevel)
                    search(searchRequest)
                }
                false -> {
                    launch {
                        if (!statusChannel.isClosedForSend)
                            statusChannel.send(SearchStatus.COMPLETED)
                        processingRequestChannel.close()
                        processedRequestChannel.close()
                        statusChannel.close()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun singleSearch(searchRequest: SearchRequest): SearchResult {
        return withTimeout(URL_PROCESS_TIMEOUT) {
            val fetchedText = searchRepository.getText(searchRequest.url)
            val entriesCount = async { fetchedText.countEntries(searchRequest.textToFind) }
            val parentUrls = async { fetchedText.extractUrls() }
            val searchResult = SearchResult(entriesCount.await(), parentUrls.await())
            mutex.withLock {
                cache.addNextLevelUrls(searchResult.parentUrls)
            }
            searchResult
        }
    }

    fun stop() {
        processingRequestChannel.close()
        processedRequestChannel.close()
        statusChannel.close()
        coroutineContext.cancelChildren()
    }

    fun receiveRequest(): ReceiveChannel<SearchRequest> = processingRequestChannel

    fun receiveResult(): ReceiveChannel<SearchModel> = processedRequestChannel

    fun receiveStatus(): ReceiveChannel<SearchStatus> = statusChannel
}