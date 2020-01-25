package com.bohdan.khristov.textsearch.ui.common.validation.rules

import com.bohdan.khristov.textsearch.ui.common.validation.Rule

class MinLengthRule(val minLength: Int, message:String) : Rule(message) {

    override fun isPassed(value: String?) = value != null && value.length >= minLength
}