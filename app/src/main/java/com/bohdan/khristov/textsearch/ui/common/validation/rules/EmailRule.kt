package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule
import java.util.regex.Pattern

class EmailRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return Pattern
                .compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE)
                .matcher(value)
                .find()
    }

    companion object {
        private const val EMAIL_REGEX = "\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b"
    }
}