package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.ISearchRepository
import com.bohdan.khristov.textsearch.data.SearchRepository
import com.bohdan.khristov.textsearch.domain.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchInteractor @Inject constructor(
    private val searchRepository: ISearchRepository
) {

    fun search(url: String, resource: (Resource<String>) -> Unit): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            resource(Resource.loading())
            try {
                resource(Resource.success(searchRepository.getText(url)))
            } catch (e: Exception) {
                e.printStackTrace()
                resource(Resource.error("Error fetching text"))
            }
        }
    }
}