package com.bohdan.khristov.textsearch.data

interface ISearchRepository {
    suspend fun getText(url: String): String
}