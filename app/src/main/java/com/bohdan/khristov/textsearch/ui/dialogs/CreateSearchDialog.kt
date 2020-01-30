package com.bohdan.khristov.textsearch.ui.dialogs

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.ui.common.validation.ValidationInput
import com.bohdan.khristov.textsearch.ui.common.validation.Validator
import com.bohdan.khristov.textsearch.ui.common.validation.inputs.InputLayoutWrapper
import com.bohdan.khristov.textsearch.ui.common.validation.rules.NumberRule
import com.bohdan.khristov.textsearch.ui.common.validation.rules.NotEmptyRule
import com.bohdan.khristov.textsearch.ui.common.validation.rules.UrlRule
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.new_search_dialog.view.*

class CreateSearchDialog(
    context: Context,
    var onPositive: ((SearchRequest) -> Unit)? = null
) : IDialog {

    private var dialog: MaterialDialog = MaterialDialog(context, BottomSheet()).noAutoDismiss()

    private val validator = Validator()

    init {
        dialog.title(R.string.new_search)

        dialog.customView(R.layout.new_search_dialog, scrollable = true, horizontalPadding = true)

        val customView = dialog.getCustomView()
        val urlEt: TextInputEditText = customView.findViewById(R.id.urlEt)
        val queryEt: TextInputEditText = customView.findViewById(R.id.textToFindEt)
        val maxUrlCountEt: TextInputEditText = customView.findViewById(R.id.maxUrlCountEt)
        val threadCountEt: TextInputEditText = customView.findViewById(R.id.threadCountEt)

        validator.addValidation(
            ValidationInput.Builder(InputLayoutWrapper(customView.urlTil))
                .addRule(NotEmptyRule(context.getString(R.string.validation_cant_be_empty)))
                .addRule(UrlRule(context.getString(R.string.validation_url)))
                .build()
        )

        validator.addValidation(
            ValidationInput.Builder(InputLayoutWrapper(customView.textToFindTil))
                .addRule(NotEmptyRule(context.getString(R.string.validation_cant_be_empty)))
                .build()
        )

        validator.addValidation(
            ValidationInput.Builder(InputLayoutWrapper(customView.maxUrlCountTil))
                .addRule(NotEmptyRule(context.getString(R.string.validation_cant_be_empty)))
                .addRule(NumberRule(context.getString(R.string.validation_number)))
                .build()
        )

        validator.addValidation(
            ValidationInput.Builder(InputLayoutWrapper(customView.threadCountTil))
                .addRule(NumberRule(context.getString(R.string.validation_number)))
                .build()
        )

        dialog.positiveButton(R.string.search) {
            if (validator.validate()) {
                val searchRequest = SearchRequest(
                    url = urlEt.text.toString(),
                    textToFind = queryEt.text.toString(),
                    maxUrlCount = maxUrlCountEt.text.toString().toInt(),
                    maxParallelUrl = threadCountEt.text.toString().toIntOrNull() ?: -1
                )
                onPositive?.invoke(searchRequest)
                dialog.dismiss()
            }
        }
        dialog.negativeButton(R.string.cancel) {
            dialog.dismiss()
        }

        dialog.onDismiss {
            urlEt.clearFocus()
            queryEt.clearFocus()
            maxUrlCountEt.clearFocus()
            threadCountEt.clearFocus()
        }
    }

    override fun show() {
        dialog.show()
    }
}