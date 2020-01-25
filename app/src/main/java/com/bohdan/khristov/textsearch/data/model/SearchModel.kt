package com.bohdan.khristov.textsearch.data.model

data class SearchModel(
    val request: SearchRequest,
    val result: SearchResult
) {
    companion object{
        fun empty(): SearchModel = SearchModel(
            SearchRequest("", "", -1, -1),
            SearchResult(-1, listOf())
        )
    }
}