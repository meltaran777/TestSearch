package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule
import java.util.regex.Pattern

class WithoutSpacesRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean = !Pattern
            .compile(SPACE_CHECK_REGEX, Pattern.CASE_INSENSITIVE)
            .matcher(value)
            .find()

    companion object {
        const val SPACE_CHECK_REGEX = "\\s"
    }
}