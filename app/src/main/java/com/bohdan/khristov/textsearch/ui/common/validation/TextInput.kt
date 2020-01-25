package com.bohdan.khristov.textsearch.ui.common.validation

interface TextInput {
    fun getText(): String
    fun setError(message: String)
    fun clear()
}