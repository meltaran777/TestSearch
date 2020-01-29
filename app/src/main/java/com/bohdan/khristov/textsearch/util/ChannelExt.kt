package com.bohdan.khristov.textsearch.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

@ExperimentalCoroutinesApi
suspend fun <T> Channel<T>.safeSend(obj: T) {
    if (!isClosedForSend) {
        send(obj)
    }
}