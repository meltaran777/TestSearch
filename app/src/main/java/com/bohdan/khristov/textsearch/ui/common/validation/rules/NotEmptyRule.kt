package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule

class NotEmptyRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return value != null && value.length > 0
    }
}