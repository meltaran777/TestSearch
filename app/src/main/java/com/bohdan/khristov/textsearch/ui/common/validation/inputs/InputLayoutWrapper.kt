package com.bohdan.khristov.textsearch.ui.common.validation.inputs

import android.text.Editable
import android.text.TextWatcher
import com.bohdan.khristov.textsearch.ui.common.validation.TextInput
import com.google.android.material.textfield.TextInputLayout

class InputLayoutWrapper(private var input: TextInputLayout) : TextInput {

    init {
        input.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    input.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun getText() = input.editText?.text.toString()

    override fun setError(message: String) {
        input.error = message
    }

    override fun clear() {
        input.error = null
    }
}