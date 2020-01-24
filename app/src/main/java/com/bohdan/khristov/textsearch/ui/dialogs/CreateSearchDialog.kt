package com.bohdan.khristov.textsearch.ui.dialogs

import android.content.Context
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.data.model.SearchRequest

//TODO: add validation for fields
class CreateSearchDialog(
    context: Context,
    var onPositive: ((SearchRequest) -> Unit)? = null
) : IDialog {

    private var dialog: MaterialDialog = MaterialDialog(context, BottomSheet())

    init {
        dialog.title(R.string.new_search)

        dialog.customView(R.layout.new_search_dialog, scrollable = true, horizontalPadding = true)

        val customView = dialog.getCustomView()
        val urlEt: EditText = customView.findViewById(R.id.urlEt)
        val queryEt: EditText = customView.findViewById(R.id.queryEt)

        dialog.positiveButton(R.string.search) {
            onPositive?.invoke(SearchRequest(urlEt.text.toString(), queryEt.text.toString(), 100))
        }
        dialog.negativeButton(R.string.cancel)

        dialog.onDismiss {
            urlEt.clearFocus()
            queryEt.clearFocus()
        }
    }

    override fun show() {
        dialog.show()
    }
}