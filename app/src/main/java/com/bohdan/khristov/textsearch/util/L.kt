package com.bohdan.khristov.textsearch.util

import android.util.Log
import com.bohdan.khristov.textsearch.BuildConfig

object L {

    var isDebug = BuildConfig.DEBUG

    fun log(tag: String, msg: String) {
        if (isDebug)
            Log.d(tag, msg)
    }
}