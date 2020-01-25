package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule

class PasswordMatchRule(val password: Password, message: String) : Rule(message) {

    override fun isPassed(value: String?): Boolean {
        return value != null && value.equals(password.getValue())
    }

    interface Password {
        fun getValue(): String
    }
}