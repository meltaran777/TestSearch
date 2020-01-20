package com.bohdan.khristov.textsearch.util

import android.util.Log
import com.bohdan.khristov.textsearch.BuildConfig

object L {
    fun log(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }
}