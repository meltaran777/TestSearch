package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor

fun CoroutineScope.stateActor(rootRequest: SearchRequest) = actor<StateMsg<*>> {

    val state = SearchState(rootRequest)

    for (msg in channel) {
        when (msg) {
            is AddRequestMsg -> {
                state.addRequest(msg.request)
                msg.response.complete(true)
            }
            is AddResultMsg -> {
                state.addResult(msg.result)
                msg.response.complete(true)

            }
            is GetInfoMsg -> {
                msg.response.complete(state.getInfo())
            }
            is SetStatusMsg -> {
                state.changeStatus(msg.status)
                msg.response.complete(state.status)
            }
            is GetStatusMsg -> {
                msg.response.complete(state.status)
            }
            is IsSendRequestEnableMsg -> {
                msg.response.complete(state.isSendRequestEnable())
            }
            is IncRequestCounterMsg -> {
                msg.response.complete(state.incRequestCounter())
            }
            is IncLevelMsg -> {
                msg.response.complete(state.incLevel())
            }
            is GetCurrentLevelRequestsMsg -> {
                msg.response.complete(state.getCurrentLevelRequests())
            }
            is GetNextLevelRequestsMsg -> {
                msg.response.complete(state.getNextLevelRequests())
            }
        }
    }
}

private class SearchState(val rootRequest: SearchRequest) {

    var status = SearchStatus.PREPARE
        private set

    var processedRequests = mutableListOf<SearchModel>()
        private set
    var inProgressRequests = mutableListOf<SearchRequest>()
        private set

    private var requestCounter = 0
    private var totalRequestCounter = 0
    private var processedRequestCounter = 0

    private var currentLevel = 0
    private var requestsByLevel: MutableMap<Int, MutableList<SearchRequest>> =
        mutableMapOf<Int, MutableList<SearchRequest>>().withDefault { mutableListOf() }

    private var totalEntries = 0
    private var progress = 0

    init {
        val requests = requestsByLevel.getValue(currentLevel)
        requests.add(rootRequest)
        requestsByLevel[currentLevel] = requests
    }

    fun addRequest(searchRequest: SearchRequest) {
        inProgressRequests.add(searchRequest)
    }

    fun addResult(result: SearchModel) {
        inProgressRequests.remove(result.request)

        if (result.result == SearchResult.empty()) {
            requestCounter--
            return
        }

        addNextLevelRequests(result.result.parentUrls.map { rootRequest.copy(url = it) })

        totalEntries += result.result.entriesCount
        progress++
        processedRequests.add(result)

        if (isSearchCompleted()) {
            changeStatus(SearchStatus.COMPLETED)
        }
    }

    private fun addNextLevelRequests(requests: List<SearchRequest>) {
        val nexLevel = currentLevel + 1
        val requestsOnNextLevel = requestsByLevel.getValue(nexLevel)
        requestsOnNextLevel.addAll(requests)
        requestsByLevel[nexLevel] = requestsOnNextLevel

        processedRequestCounter++
        totalRequestCounter++
        totalRequestCounter += requests.size - 1
    }

    private fun isSearchCompleted(): Boolean {
        return processedRequestCounter >= rootRequest.maxUrlCount || totalRequestCounter == processedRequestCounter
    }

    fun getInfo(): SearchInfo {
        return SearchInfo(
            progress = progress,
            entriesCount = totalEntries,
            processedRequests = processedRequests.toMutableList(),
            inProgressRequests = inProgressRequests.toMutableList()
        )
    }

    fun changeStatus(status: SearchStatus) {
        this.status = status
    }

    fun getCurrentLevelRequests(): List<SearchRequest> {
        return requestsByLevel.getValue(currentLevel)
    }

    fun getNextLevelRequests(): List<SearchRequest> {
        val nextLevel = currentLevel + 1
        return requestsByLevel.getValue(nextLevel)
    }

    fun isSendRequestEnable(): Boolean {
        return requestCounter < rootRequest.maxUrlCount
    }

    fun incRequestCounter() = requestCounter++

    fun incLevel() = currentLevel++
}