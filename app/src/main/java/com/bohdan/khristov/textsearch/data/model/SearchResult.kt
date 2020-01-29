package com.bohdan.khristov.textsearch.data.model

data class SearchResult(
    var entriesCount: Int,
    var parentUrls: List<String>
) {
    companion object {
        fun empty(): SearchResult = SearchResult(-1, listOf())
    }
}