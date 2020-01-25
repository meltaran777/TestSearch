package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule

class UsernameRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return value?.length!! > MIN_LENGTH
    }

    companion object {
        const val MIN_LENGTH = 5
    }
}