package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchResult
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.domain.common.Resource
import com.bohdan.khristov.textsearch.util.L
import com.bohdan.khristov.textsearch.util.entriesCount
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.*
import javax.inject.Inject

class SearchInteractor @Inject constructor(
    private val searchRepository: ISearchRepository
) {

    private var processedUrlCounter = 0
    private var currentLevel = 0
    private val urlsByLevel =
        mutableMapOf<Int, MutableList<String>>().withDefault { mutableListOf() }

    fun fullSearch(
        searchRequest: SearchRequest,
        resource: (Resource<SearchModel>) -> Unit
    ): Job {
        processedUrlCounter = 0
        currentLevel = 0
        urlsByLevel.clear()
        return search(searchRequest,resource)
    }

    private fun search(searchRequest: SearchRequest,
                           resource: (Resource<SearchModel>) -> Unit): Job {
        return GlobalScope.launch(Dispatchers.Default) {
            resource(Resource.loading())
            try {
                if (processedUrlCounter >= searchRequest.maxUrlCount)
                    return@launch

                if (currentLevel == 0) {
                    val searchResult = singleSearch(searchRequest)
                    resource(Resource.success(SearchModel(searchRequest, searchResult)))
                }

                val parentUrls = urlsByLevel.getValue(currentLevel)
                parentUrls.forEachIndexed { index, parentUrl ->
                    if (processedUrlCounter < searchRequest.maxUrlCount) {
                        val parentRequest = searchRequest.copy(url = parentUrl)
                        val parentResult = singleSearch(parentRequest)
                        resource(Resource.success(SearchModel(parentRequest, parentResult)))
                    }
                }

                currentLevel += 1
                val firstUrlOnNextLevel = urlsByLevel.getValue(currentLevel).firstOrNull()
                if (firstUrlOnNextLevel != null) {
                    val request = searchRequest.copy(url = firstUrlOnNextLevel)
                    search(request, resource)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                resource(Resource.error("Error while search"))
            }
        }
    }

    private suspend fun singleSearch(searchRequest: SearchRequest) = GlobalScope.async(Dispatchers.Default) {
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

        processedUrlCounter++

        return@async searchResult
    }.await()


    fun stopSearch() {}
}