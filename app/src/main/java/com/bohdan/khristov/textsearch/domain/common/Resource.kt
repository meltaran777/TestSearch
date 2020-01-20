package com.bohdan.khristov.textsearch.domain.common

class Resource<T>(
    val status: Status,
    val data: T?,
    val message: String?) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(message: String, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        fun <T> error(): Resource<T> {
            return Resource(Status.ERROR, null, null)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null)
        }
    }

    enum class Status {
        SUCCESS, ERROR, LOADING
    }
}