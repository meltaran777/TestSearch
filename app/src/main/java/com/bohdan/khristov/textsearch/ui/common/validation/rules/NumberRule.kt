package com.bohdan.khristov.textsearch.ui.common.validation.rules

import androidx.core.text.isDigitsOnly
import com.bohdan.khristov.textsearch.ui.common.validation.Rule

class NumberRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return value != null && value.isDigitsOnly()
    }
}