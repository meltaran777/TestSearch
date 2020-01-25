package com.bohdan.khristov.textsearch.ui.common.validation.inputs

import android.widget.TextView
import com.bohdan.khristov.textsearch.ui.common.validation.TextInput

class TextViewWrapper(private var input: TextView) : TextInput {

    override fun getText() = input.text.toString()

    override fun setError(message: String) {
        input.error = message
    }

    override fun clear() {
        input.error = null
    }
}