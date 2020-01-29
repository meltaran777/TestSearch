package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchInfo
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import com.bohdan.khristov.textsearch.util.safeSend
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

class SearchState(scope: CoroutineScope, val rootRequest: SearchRequest) :
    CoroutineScope by scope {

    val processingRequestChannel = Channel<SearchRequest>()
    val processedRequestChannel = Channel<SearchModel>()
    val statusChannel = Channel<SearchStatus>()

    private val mutex = Mutex()

    private var status = SearchStatus.IN_PROGRESS

    private val requestCounter: AtomicInteger = AtomicInteger(0)
    private val totalUrlsCounter: AtomicInteger = AtomicInteger(0)
    private val processedUrlCounter: AtomicInteger = AtomicInteger(0)
    private val currentLevel: AtomicInteger = AtomicInteger(0)

    private val urlsByLevel: MutableMap<Int, MutableList<String>> =
        mutableMapOf<Int, MutableList<String>>().withDefault { mutableListOf() }

    init {
        val urls = urlsByLevel.getValue(currentLevel.get())
        urls.add(rootRequest.url)
        urlsByLevel[currentLevel.get()] = urls
    }

    suspend fun addProcessingRequest(searchRequest: SearchRequest) {
        processingRequestChannel.safeSend(searchRequest)
    }

    suspend fun addSearchResult(result: SearchModel) {
        addNextLevelUrls(result.result.parentUrls)
        processedRequestChannel.safeSend(result)
        if (isSearchCompleted()) {
            changeStatus(SearchStatus.COMPLETED)
        }
    }

    private suspend fun addNextLevelUrls(parentUrls: List<String>) {
         mutex.withLock {
            val nexLevel = currentLevel.get() + 1
            val urlOnNextLevel = urlsByLevel.getValue(nexLevel)
            urlOnNextLevel.addAll(parentUrls)
            urlsByLevel[nexLevel] = urlOnNextLevel

            processedUrlCounter.incrementAndGet()
            totalUrlsCounter.incrementAndGet()
            totalUrlsCounter.addAndGet(parentUrls.size - 1)
        }
    }

    private fun isSearchCompleted(): Boolean {
        return processedUrlCounter.get() >= rootRequest.maxUrlCount || totalUrlsCounter.get() == processedUrlCounter.get()
    }

    suspend fun changeStatus(status: SearchStatus) {
        this@SearchState.status = status
        statusChannel.safeSend(status)
        if (status == SearchStatus.COMPLETED) {
            close()
        }
    }

    fun getInfo(): SearchInfo {
        return SearchInfo(0, 0, listOf())
    }

    suspend fun getCurrentLevelUrls(): MutableList<String> {
        return mutex.withLock {
            urlsByLevel.getValue(currentLevel.get())
        }
    }

    suspend fun getNextLevelUrls(): MutableList<String> {
        return mutex.withLock {
            val nextLevel = currentLevel.get() + 1
            urlsByLevel.getValue(nextLevel)
        }
    }

    fun getLevel() = currentLevel.get()

    fun isSendRequestEnable(): Boolean = requestCounter.get() < rootRequest.maxUrlCount

    fun incRequest() = requestCounter.incrementAndGet()

    fun incLevel() = currentLevel.incrementAndGet()

    fun clean() {
        processedUrlCounter.set(0)
        requestCounter.set(0)
        totalUrlsCounter.set(0)
        currentLevel.set(0)
        urlsByLevel.clear()
    }

    fun close() {
        processingRequestChannel.close()
        processedRequestChannel.close()
        statusChannel.close()
    }

    fun decRequest() = requestCounter.decrementAndGet()

}