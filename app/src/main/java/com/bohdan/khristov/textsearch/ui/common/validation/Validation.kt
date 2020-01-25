package com.bohdan.khristov.textsearch.ui.common.validation

interface Validation {

    fun validate(): Boolean

    fun clear()

    fun getValue(): String
}