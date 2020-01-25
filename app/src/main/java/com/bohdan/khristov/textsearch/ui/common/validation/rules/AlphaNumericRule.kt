package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule
import java.util.regex.Pattern

class AlphaNumericRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return Pattern
                .compile(ALPHA_NUMERIC_REGEX, Pattern.CASE_INSENSITIVE)
                .matcher(value)
                .find()
    }

    companion object {
        private const val ALPHA_NUMERIC_REGEX = "^[A-Za-z_][A-Za-z\\d_]*\$"
    }
}