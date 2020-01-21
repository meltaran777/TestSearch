package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchResult
import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.domain.common.Resource
import com.bohdan.khristov.textsearch.util.L
import com.bohdan.khristov.textsearch.util.entriesCount
import com.bohdan.khristov.textsearch.util.extractUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchInteractor @Inject constructor(
    private val searchRepository: ISearchRepository
) {

    fun search(
        searchRequest: SearchRequest,
        resource: (Resource<SearchModel>) -> Unit
    ): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            resource(Resource.loading())
            try {
                val fetchedText = searchRepository.getText(searchRequest.rootUrl)
                val textEntries = fetchedText.entriesCount(searchRequest.textToFind)
                val parentUrls = fetchedText.extractUrls()
                val searchModel = SearchModel(searchRequest, SearchResult(textEntries, parentUrls))
                L.log("SearchInteractor","searchModel = $searchModel")
                resource(Resource.success(searchModel)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                resource(Resource.error("Error while search"))
            }
        }
    }
}