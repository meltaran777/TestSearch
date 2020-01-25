package com.bohdan.khristov.textsearch.util

import android.view.View

fun View.toggleVisibility(isVisible: Boolean) {
    if (isVisible) this.visibility = View.VISIBLE else this.visibility = View.GONE
}