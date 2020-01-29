package com.bohdan.khristov.textsearch.data.model

data class SearchRequest(
    val url: String,
    val textToFind: String,
    val maxUrlCount: Int,
    val threadCount: Int = -1
){
    companion object {
        fun empty(): SearchRequest = SearchRequest("", "", -1, -1)
    }
}