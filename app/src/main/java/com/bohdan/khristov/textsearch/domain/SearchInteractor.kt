package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchResult
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.util.L
import com.bohdan.khristov.textsearch.util.entriesCount
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SearchInteractor @Inject constructor(
    private val searchRepository: ISearchRepository
) {

    private lateinit var context: CoroutineContext

    private var maxUrlsCount = 0

    private var totalUrlsCounter = 0
    private var processedUrlCounter = 0

    private var currentLevel = 0
    private val urlsByLevel =
        mutableMapOf<Int, MutableList<String>>().withDefault { mutableListOf() }

    @ObsoleteCoroutinesApi
    fun fullSearch(
        searchRequest: SearchRequest,
        onStartProcessingUrl: (SearchRequest) -> Unit,
        onUrlProcessed: (SearchModel) -> Unit,
        onCompleted: () -> Unit
    ): Job {
        context =
            if (searchRequest.threadCount > Runtime.getRuntime().availableProcessors() || searchRequest.threadCount <= 0)
                Dispatchers.Default
            else newFixedThreadPoolContext(searchRequest.threadCount, "fixed-thread-pool")
        return GlobalScope.launch(context) {
            processedUrlCounter = 0
            currentLevel = 0
            maxUrlsCount = searchRequest.maxUrlCount
            urlsByLevel.clear()
            search(searchRequest, onStartProcessingUrl, onUrlProcessed, onCompleted)
        }
    }

    private suspend fun search(
        searchRequest: SearchRequest,
        onStartProcessingUrl: (SearchRequest) -> Unit,
        onUrlProcessed: (SearchModel) -> Unit,
        onCompleted: () -> Unit
    ) {
        try {
            if (currentLevel == 0) {
                onStartProcessingUrl.invoke(searchRequest)
                val searchResult = singleSearch(searchRequest)
                onUrlProcessed.invoke(SearchModel(searchRequest, searchResult))
            }

            val parentUrls = urlsByLevel.getValue(currentLevel)
            parentUrls.forEachIndexed { index, parentUrl ->
                if (!isSearchCompleted()) {
                    val parentRequest = searchRequest.copy(url = parentUrl)
                    onStartProcessingUrl.invoke(parentRequest)
                    val parentResult = singleSearch(parentRequest)
                    onUrlProcessed.invoke(SearchModel(parentRequest, parentResult))
                } else {
                    onCompleted.invoke()
                    return
                }
            }

            currentLevel += 1
            val firstUrlOnNextLevel = urlsByLevel.getValue(currentLevel).firstOrNull()
            if (firstUrlOnNextLevel != null) {
                val request = searchRequest.copy(url = firstUrlOnNextLevel)
                search(request, onStartProcessingUrl, onUrlProcessed, onCompleted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isSearchCompleted(): Boolean {
        return processedUrlCounter >= maxUrlsCount || totalUrlsCounter == processedUrlCounter
    }

    private suspend fun singleSearch(searchRequest: SearchRequest) =
        GlobalScope.async(context) {
            val fetchedText = searchRepository.getText(searchRequest.url)
            val textEntries = fetchedText.entriesCount(searchRequest.textToFind)
            val parentUrls = fetchedText.extractUrls()
            val searchResult = SearchResult(textEntries, parentUrls)

            L.log("SearchInteractor", "currentLevel = $currentLevel")
            L.log("SearchInteractor", "searchRequest = $searchRequest")
            L.log("SearchInteractor", "searchResult = $searchResult")
            L.log("SearchInteractor", "----------------------------------------------------")
            L.log("SearchInteractor", "====================================================")
            L.log("SearchInteractor", "----------------------------------------------------")

            val nexLevel = currentLevel + 1
            val urlOnNextLevel: MutableList<String> = urlsByLevel.getValue(nexLevel)
            urlOnNextLevel.addAll(parentUrls)
            urlsByLevel[nexLevel] = urlOnNextLevel

            processedUrlCounter += 1
            totalUrlsCounter += 1
            totalUrlsCounter += parentUrls.size - 1

            return@async searchResult
        }.await()
}