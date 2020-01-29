package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.*
import com.bohdan.khristov.textsearch.util.safeSend
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class SearchState(val rootRequest: SearchRequest) {

    val inProgressRequestsChannel = Channel<List<SearchRequest>>()
    val infoChannel = Channel<SearchInfo>()
    val statusChannel = Channel<SearchStatus>()

    var status = SearchStatus.PREPARE
        private set

    private val mutex = Mutex()

    private val requestCounter: AtomicInteger = AtomicInteger(0)
    private val totalRequestCounter: AtomicInteger = AtomicInteger(0)
    private val processedRequestCounter: AtomicInteger = AtomicInteger(0)

    private val currentLevel: AtomicInteger = AtomicInteger(0)
    private val requestsByLevel: MutableMap<Int, MutableList<SearchRequest>> =
        mutableMapOf<Int, MutableList<SearchRequest>>().withDefault { mutableListOf() }

    private val totalEntries = AtomicInteger(0)
    private val progress = AtomicInteger(0)
    private var processedRequests = Collections.synchronizedList(mutableListOf<SearchModel>())
    private var inProgressRequests = Collections.synchronizedList(mutableListOf<SearchRequest>())

    init {
        val urls = requestsByLevel.getValue(currentLevel.get())
        urls.add(rootRequest)
        requestsByLevel[currentLevel.get()] = urls
    }

    suspend fun addInProgressRequest(searchRequest: SearchRequest) {
        mutex.withLock {
            inProgressRequests.add(searchRequest)
            inProgressRequestsChannel.safeSend(inProgressRequests)
        }
    }

    suspend fun addSearchResult(result: SearchModel) {
        if (result.result == SearchResult.empty()) {
            requestCounter.decrementAndGet()
            return
        }
        mutex.withLock {
            inProgressRequests.remove(result.request)
            inProgressRequestsChannel.safeSend(inProgressRequests)

            addNextLevelRequests(result.result.parentUrls.map { rootRequest.copy(url = it) })

            totalEntries.addAndGet(result.result.entriesCount)
            progress.incrementAndGet()
            processedRequests.add(result)

            infoChannel.safeSend(getInfo())

            if (isSearchCompleted()) {
                changeStatus(SearchStatus.COMPLETED)
            }
        }
    }

    private fun addNextLevelRequests(requests: List<SearchRequest>) {
        val nexLevel = currentLevel.get() + 1
        val requestsOnNextLevel = requestsByLevel.getValue(nexLevel)
        requestsOnNextLevel.addAll(requests)
        requestsByLevel[nexLevel] = requestsOnNextLevel

        processedRequestCounter.incrementAndGet()
        totalRequestCounter.incrementAndGet()
        totalRequestCounter.addAndGet(requests.size - 1)
    }

    private fun isSearchCompleted(): Boolean {
        return processedRequestCounter.get() >= rootRequest.maxUrlCount || totalRequestCounter.get() == processedRequestCounter.get()
    }

    private fun getInfo(): SearchInfo {
        return SearchInfo(
            progress = progress.get(),
            entriesCount = totalEntries.get(),
            processedRequests = processedRequests
        )
    }

    suspend fun changeStatus(status: SearchStatus) {
        this@SearchState.status = status
        statusChannel.safeSend(status)
    }

    suspend fun getCurrentLevelRequests(): List<SearchRequest> {
        return mutex.withLock {
            requestsByLevel.getValue(currentLevel.get())
        }
    }

    suspend fun getNextLevelRequests(): List<SearchRequest> {
        return mutex.withLock {
            val nextLevel = currentLevel.get() + 1
            requestsByLevel.getValue(nextLevel)
        }
    }

    fun isSendRequestEnable(): Boolean = requestCounter.get() < rootRequest.maxUrlCount

    fun incRequestCounter() = requestCounter.incrementAndGet()

    fun incLevel() = currentLevel.incrementAndGet()

    fun close() {
        inProgressRequestsChannel.close()
        infoChannel.close()
        statusChannel.close()
    }
}