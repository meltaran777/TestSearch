package com.bohdan.khristov.textsearch.ui.common.validation.inputs

import com.bohdan.khristov.textsearch.ui.common.validation.TextInput
import com.google.android.material.textfield.TextInputEditText

class EditTextWrapper(private var input: TextInputEditText) : TextInput {

    override fun getText() = input.text.toString()

    override fun setError(message: String) {
        input.error = message
    }

    override fun clear() {
        input.error = null
    }
}