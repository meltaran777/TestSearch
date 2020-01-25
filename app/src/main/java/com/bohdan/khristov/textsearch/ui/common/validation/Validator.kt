package com.bohdan.khristov.textsearch.ui.common.validation

import com.bohdan.khristov.textsearch.ui.common.validation.Validation
import com.bohdan.khristov.textsearch.ui.common.validation.ValidationInput

class Validator {
    private var validations = ArrayList<Validation>()

    fun validate(): Boolean {
        clearErrors()

        for (validation in validations) {
            if (!validation.validate())
                return false
        }
        return true
    }

    fun addValidation(validation: ValidationInput) {
        if ( !validations.contains(validation)) {
            validations.add(validation)
        }
    }

    fun removeValidation(validation: ValidationInput) {
        if ( !validations.contains(validation)) {
            validations.remove(validation)
        }
    }

    fun clear() {
        validations.clear()
    }

    private fun clearErrors() {
        for (validation in validations) {
            validation.clear()
        }
    }
}