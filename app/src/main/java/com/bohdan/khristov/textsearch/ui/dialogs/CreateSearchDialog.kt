package com.bohdan.khristov.textsearch.ui.dialogs

import android.content.Context
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bohdan.khristov.textsearch.R

class CreateSearchDialog(context: Context) : IDialog {

    private var dialog: MaterialDialog = MaterialDialog(context, BottomSheet())

    init {
        dialog.title(R.string.new_search)
        dialog.positiveButton(R.string.search)
        dialog.negativeButton(R.string.cancel)
        dialog.customView(R.layout.new_search_dialog, scrollable = true, horizontalPadding = true)
        dialog.onDismiss {
            val customView = it.getCustomView()
            val urlEt: EditText = customView.findViewById(R.id.urlEt)
            val queryEt: EditText = customView.findViewById(R.id.queryEt)
            urlEt.clearFocus()
            queryEt.clearFocus()
        }
    }

    override fun show() {
        dialog.show()
    }
}