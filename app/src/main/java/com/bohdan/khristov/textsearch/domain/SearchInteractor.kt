package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchResult
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.L
import com.bohdan.khristov.textsearch.util.entriesCount
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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

    private var requestCounter = AtomicInteger()
    private var totalUrlsCounter = AtomicInteger()
    private var processedUrlCounter = AtomicInteger()

    private var maxUrlsCount = 0

    private var currentLevel = 0
    private var urlsByLevel =
        mutableMapOf<Int, MutableList<String>>().withDefault { mutableListOf() }

    @ObsoleteCoroutinesApi
    fun search1(
        searchRequest: SearchRequest,
        onStartProcessingUrl: (SearchRequest) -> Unit,
        onUrlProcessed: (SearchModel) -> Unit,
        onCompleted: () -> Unit
    ): Job {

        processedUrlCounter = AtomicInteger(0)
        requestCounter = AtomicInteger(0)
        totalUrlsCounter = AtomicInteger(0)
        currentLevel = 0
        maxUrlsCount = searchRequest.maxUrlCount
        urlsByLevel.clear()

        return launch {
            search(searchRequest, onStartProcessingUrl, onUrlProcessed, onCompleted)
        }
    }

    fun stop() {
        coroutineContext.cancelChildren()
    }

    private suspend fun search(
        searchRequest: SearchRequest,
        onStartProcessingUrl: (SearchRequest) -> Unit,
        onUrlProcessed: (SearchModel) -> Unit,
        onCompleted: () -> Unit
    ) {
        try {
            if (currentLevel == 0) {
                singleSearch(searchRequest, onStartProcessingUrl, onUrlProcessed)
            }

            val channel: Channel<SearchRequest> = Channel()
            launch() {
                val parentUrls = urlsByLevel.getValue(currentLevel)
                parentUrls.forEachIndexed { index, parentUrl ->
                    if (requestCounter.get() < maxUrlsCount) {
                        val parentRequest = searchRequest.copy(url = parentUrl)
                        L.log("SearchDebug", "Request $requestCounter = $parentRequest")
                        channel.send(parentRequest)
                    }
                    requestCounter.incrementAndGet()
                }
                channel.close()
            }

            val jobs = mutableListOf<Job>()
            for (i in 0..searchRequest.threadCount) {
                jobs.add(launch {
                    for (request in channel) {
                        singleSearch(request, onStartProcessingUrl, onUrlProcessed)
                    }
                })
            }
            jobs.forEach { it.join() }

            val firstUrlOnNextLevel = mutex.withLock {
                currentLevel += 1
                urlsByLevel.getValue(currentLevel).firstOrNull()
            }
            if (firstUrlOnNextLevel != null) {
                val request = searchRequest.copy(url = firstUrlOnNextLevel)
                search(request, onStartProcessingUrl, onUrlProcessed, onCompleted)
            } else {
                onCompleted.invoke()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun singleSearch(
        searchRequest: SearchRequest,
        onStartProcessingUrl: (SearchRequest) -> Unit,
        onUrlProcessed: (SearchModel) -> Unit
    ): SearchResult {
        return async {
            withTimeout(10_000) {
                L.log("SearchDebug", "singleSearch $searchRequest")
                mutex.withLock {
                    onStartProcessingUrl.invoke(searchRequest)
                }

                val fetchedText = async {
                    searchRepository.getText(searchRequest.url)
                }.await()

                val textEntries = fetchedText.entriesCount(searchRequest.textToFind)
                val parentUrls = fetchedText.extractUrls()
                val searchResult = SearchResult(textEntries, parentUrls)

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

                val searchModel = SearchModel(searchRequest, searchResult)
                L.log("FinishSearch", "Finish search $processedUrlCounter")
                onUrlProcessed.invoke(searchModel)

                searchResult
            }
        }.await()
    }

/*    private fun isSearchCompleted(): Boolean {
        return processedUrlCounter >= maxUrlsCount || totalUrlsCounter == processedUrlCounter
    }*/
}