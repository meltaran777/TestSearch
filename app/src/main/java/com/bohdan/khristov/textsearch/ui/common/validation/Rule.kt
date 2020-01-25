package com.bohdan.khristov.textsearch.ui.common.validation

abstract class Rule(var error: String = "Invalid input") {
    abstract fun isPassed(value: String?): Boolean
}