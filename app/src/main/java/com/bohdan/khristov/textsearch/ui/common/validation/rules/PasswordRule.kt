package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule
import java.util.regex.Pattern

class PasswordRule(message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return Pattern
                .compile(PASSWORD_REGEX, Pattern.CASE_INSENSITIVE)
                .matcher(value)
                .find()
    }

    companion object {
        const val PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"
    }
}