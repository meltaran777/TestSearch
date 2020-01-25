package com.bohdan.khristov.textsearch.ui.common.validation

class ValidationInput private constructor(builder: Builder) : Validation {
    var rules: List<Rule>
    var input: TextInput

    init {
        rules = builder.rules
        input = builder.input
    }

    override fun validate(): Boolean {
        val value = input.getText()

        for (rule in rules) {
            if (!rule.isPassed(value)) {
                input.setError(rule.error)

                return false
            }
        }
        return true
    }

    override fun clear() {
        input.clear()
    }

    override fun getValue(): String {
        return input.getText()
    }

    class Builder(val input: TextInput) {
        var rules = ArrayList<Rule>()

        fun addRule(rule: Rule): Builder {
            rules.add(rule)
            return this
        }

        fun build(): ValidationInput {
            return ValidationInput(this)
        }
    }
}