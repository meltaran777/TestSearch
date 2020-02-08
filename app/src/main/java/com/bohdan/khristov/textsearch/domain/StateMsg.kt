package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchInfo
import com.bohdan.khristov.textsearch.data.model.SearchModel
import com.bohdan.khristov.textsearch.data.model.SearchRequest
import com.bohdan.khristov.textsearch.data.model.SearchStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

sealed class StateMsg<T>(val response: CompletableDeferred<T> = CompletableDeferred())

class AddRequestMsg(
    val request: SearchRequest,
    response: CompletableDeferred<Boolean> = CompletableDeferred()
) :
    StateMsg<Boolean>(response)

class AddResultMsg(
    val result: SearchModel,
    response: CompletableDeferred<Boolean> = CompletableDeferred()
) :
    StateMsg<Boolean>(response)

class GetInfoMsg(response: CompletableDeferred<SearchInfo> = CompletableDeferred()) :
    StateMsg<SearchInfo>(response)

class SetStatusMsg(
    val status: SearchStatus,
    response: CompletableDeferred<SearchStatus> = CompletableDeferred()
) :
    StateMsg<SearchStatus>(response)

class GetStatusMsg(response: CompletableDeferred<SearchStatus> = CompletableDeferred()) :
    StateMsg<SearchStatus>(response)

class IsSendRequestEnableMsg(response: CompletableDeferred<Boolean> = CompletableDeferred()) :
    StateMsg<Boolean>(response)

class IncRequestCounterMsg(response: CompletableDeferred<Int> = CompletableDeferred()) :
    StateMsg<Int>(response)

class IncLevelMsg(response: CompletableDeferred<Int> = CompletableDeferred()) :
    StateMsg<Int>(response)

class GetCurrentLevelRequestsMsg(response: CompletableDeferred<List<SearchRequest>> = CompletableDeferred()) :
    StateMsg<List<SearchRequest>>(response)

class GetNextLevelRequestsMsg(response: CompletableDeferred<List<SearchRequest>> = CompletableDeferred()) :
    StateMsg<List<SearchRequest>>(response)

suspend fun <T> SendChannel<StateMsg<*>>.sendWithResult(msg: StateMsg<T>): T {
    if(!isClosedForSend)
        this.send(msg)
    return msg.response.await()
}