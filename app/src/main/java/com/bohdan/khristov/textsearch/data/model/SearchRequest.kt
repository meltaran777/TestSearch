package com.bohdan.khristov.textsearch.data.model

data class SearchRequest(
    val url: String,
    val textToFind: String,
    val maxUrlCount: Int,
    val maxParallelUrl: Int
){
    companion object {
        fun empty(): SearchRequest = SearchRequest("", "", -1, -1)
    }
}