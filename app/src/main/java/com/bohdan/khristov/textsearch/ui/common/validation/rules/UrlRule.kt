package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule
import com.bohdan.khristov.textsearch.util.URL_REGEX
import java.util.regex.Pattern

class UrlRule(val message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return Pattern
            .compile(URL_REGEX, Pattern.CASE_INSENSITIVE)
            .matcher(value.toString())
            .find()
    }
}