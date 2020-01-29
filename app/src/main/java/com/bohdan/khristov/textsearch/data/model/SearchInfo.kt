package com.bohdan.khristov.textsearch.data.model

data class SearchInfo(
    val progress: Int,
    val entriesCount: Int,
    val processedRequest: List<SearchModel>
)