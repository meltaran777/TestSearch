package com.bohdan.khristov.textsearch.util

import java.util.regex.Pattern

fun String.entriesCount(text: String): Int {
    return this.windowed(text.length) {
        if (it == text)
            1
        else
            0
    }.sum()
}

fun String.extractUrls(): List<String> {
    val containedUrls = ArrayList<String>()
    val urlRegex =
        "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
    val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
    val urlMatcher = pattern.matcher(this)

    while (urlMatcher.find()) {
        containedUrls.add(
            this.substring(
                urlMatcher.start(0),
                urlMatcher.end(0)
            )
        )
    }

    return containedUrls
}