package com.bohdan.khristov.textsearch.data.repository

interface ISearchRepository {
    suspend fun getText(url: String): String
}