package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.*
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.countEntries
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val REQUEST_PROCESS_TIMEOUT = 5_000L

class SearchInteractor @Inject constructor(private val searchRepository: ISearchRepository) :
    CoroutineScope {

    interface Listener {
        fun onStatusChanged(status: SearchStatus)
        fun onInfoChanged(info: SearchInfo)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val job = SupervisorJob()
    private var stateActor: SendChannel<StateMsg<*>>? = null

    private var listener: Listener? = null

    @ObsoleteCoroutinesApi
    fun search(request: SearchRequest, listener: Listener): Job {
        return launch {
            this@SearchInteractor.listener = listener
            stateActor = stateActor(request)
            stateActor!!.sendWithResult(SetStatusMsg(SearchStatus.IN_PROGRESS))?.let { status ->
                listener.onStatusChanged(status)
            }
            wideSearch(request)
        }
    }

    private suspend fun wideSearch(rootRequest: SearchRequest) {
        try {
            val requestChannel = produce {
                val requests = stateActor!!.sendWithResult(GetCurrentLevelRequestsMsg())
                if (requests != null) {
                    for (request in requests) {
                        val isEnable = stateActor!!.sendWithResult(IsSendRequestEnableMsg())
                        if (isEnable == true) {
                            send(request)
                            stateActor!!.sendWithResult(IncRequestCounterMsg())
                        }
                    }
                }
            }

            val jobs = mutableListOf<Job>()
            for (i in 1..rootRequest.maxParallelUrl) {
                jobs.add(launch {
                    for (request in requestChannel) {
                        stateActor!!.sendWithResult(AddRequestMsg(request))
                        updateInfo()

                        val result = singleSearch(request) ?: SearchResult.empty()
                        stateActor!!.sendWithResult(AddResultMsg(SearchModel(request, result)))
                        updateInfo()
                        updateStatus()
                    }
                })
            }
            jobs.forEach { it.join() }

            val firstRequestOnNextLevel =
                stateActor!!.sendWithResult(GetNextLevelRequestsMsg())?.firstOrNull()
            when (firstRequestOnNextLevel != null) {
                true -> {
                    stateActor!!.sendWithResult(IncLevelMsg())
                    wideSearch(firstRequestOnNextLevel)
                }
                false -> {
                    stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun updateInfo() {
        val info = stateActor!!.sendWithResult(GetInfoMsg())
        if (info != null) {
            listener?.onInfoChanged(info)
        }
    }

    private suspend fun updateStatus() {
        val status = stateActor!!.sendWithResult(GetStatusMsg())
        if (status != null) {
            listener?.onStatusChanged(status)
        }
    }

    private suspend fun singleSearch(searchRequest: SearchRequest): SearchResult? {
        return withTimeoutOrNull(REQUEST_PROCESS_TIMEOUT) {
            val fetchedText = searchRepository.getText(searchRequest.url)
            withContext(Dispatchers.Default + job) {
                val entriesCount = async { fetchedText.countEntries(searchRequest.textToFind) }
                val parentUrls = async { fetchedText.extractUrls() }
                val searchResult = SearchResult(entriesCount.await(), parentUrls.await())
                searchResult
            }
        }
    }

    fun stop(): Job {
        return launch {
            launch {
                val status = stateActor!!.sendWithResult(SetStatusMsg(SearchStatus.COMPLETED))
                if (status != null) {
                    listener?.onStatusChanged(status)
                }
            }.join()
            close()
        }
    }

    private fun close() {
        listener = null
        stateActor?.close()
        coroutineContext.cancelChildren()
    }
}