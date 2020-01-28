package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchResult
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.L
import com.bohdan.khristov.textsearch.util.countEntries
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

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

    private var requestCounter = AtomicInteger()
    private var totalUrlsCounter = AtomicInteger()
    private var processedUrlCounter = AtomicInteger()

    private var maxUrlsCount = 0

    private var currentLevel = 0
    private var urlsByLevel =
        mutableMapOf<Int, MutableList<String>>().withDefault { mutableListOf() }

    @ObsoleteCoroutinesApi
    fun search1(searchRequest: SearchRequest) {
        processingRequestChannel = Channel()
        processedRequestChannel = Channel()
        statusChannel = Channel()

        processedUrlCounter = AtomicInteger(0)
        requestCounter = AtomicInteger(0)
        totalUrlsCounter = AtomicInteger(0)

        currentLevel = 0
        maxUrlsCount = searchRequest.maxUrlCount
        urlsByLevel.clear()
        launch {
            search(searchRequest)
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

    private suspend fun search(request: SearchRequest) {
        try {
            if (currentLevel == 0) {
                launch { processingRequestChannel.send(request) }
                val result = singleSearch(request)
                val model = SearchModel(request, result)
                launch { processedRequestChannel.send(model) }
            }

            val channel: Channel<SearchRequest> = Channel()
            launch {
                val parentUrls = urlsByLevel.getValue(currentLevel)
                parentUrls.forEachIndexed { index, parentUrl ->
                    if (requestCounter.get() < maxUrlsCount) {
                        val parentRequest = request.copy(url = parentUrl)
                        channel.send(parentRequest)
                    }
                    requestCounter.incrementAndGet()
                }
                channel.close()
            }

            val jobs = mutableListOf<Job>()
            for (i in 0..request.threadCount) {
                jobs.add(launch {
                    for (searchRequest in channel) {
                        launch { processingRequestChannel.send(searchRequest) }
                        val searchResult = singleSearch(searchRequest)
                        val searchModel = SearchModel(searchRequest, searchResult)
                        launch { processedRequestChannel.send(searchModel) }
                    }
                })
            }
            jobs.forEach { it.join() }

            val firstUrlOnNextLevel = mutex.withLock {
                currentLevel += 1
                urlsByLevel.getValue(currentLevel).firstOrNull()
            }
            if (firstUrlOnNextLevel != null) {
                val searchRequest = request.copy(url = firstUrlOnNextLevel)
                search(searchRequest)
            } else {
                launch {
                    statusChannel.send(SearchStatus.COMPLETED)
                    processingRequestChannel.close()
                    processedRequestChannel.close()
                    statusChannel.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun singleSearch(searchRequest: SearchRequest): SearchResult {
        return async {
            withTimeout(10_000) {
                val fetchedText = async { searchRepository.getText(searchRequest.url) }.await()
                val entriesCount = fetchedText.countEntries(searchRequest.textToFind)
                val parentUrls = fetchedText.extractUrls()
                val searchResult = SearchResult(entriesCount, parentUrls)

                L.log("SingleSearch", "currentLevel = $currentLevel")
                L.log("SingleSearch", "searchRequest = $searchRequest")
                L.log("SingleSearch", "searchResult = $searchResult")
                L.log("SingleSearch", "----------------------------------------------------")
                L.log("SingleSearch", "====================================================")
                L.log("SingleSearch", "----------------------------------------------------")

                mutex.withLock {
                    val nexLevel = currentLevel + 1
                    val urlOnNextLevel: MutableList<String> = urlsByLevel.getValue(nexLevel)
                    urlOnNextLevel.addAll(parentUrls)
                    urlsByLevel[nexLevel] = urlOnNextLevel
                }

                processedUrlCounter.incrementAndGet()
                totalUrlsCounter.incrementAndGet()
                totalUrlsCounter.addAndGet(parentUrls.size - 1)

                searchResult
            }
        }.await()
    }

/*    private fun isSearchCompleted(): Boolean {
        return processedUrlCounter >= maxUrlsCount || totalUrlsCounter == processedUrlCounter
    }*/
}