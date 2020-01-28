package com.bohdan.khristov.textsearch.domain

import com.bohdan.khristov.textsearch.data.model.SearchRequest
import java.util.concurrent.atomic.AtomicInteger

class SearchCache(request: SearchRequest) {
    private var maxUrlsCount: Int = request.maxUrlCount
    private val requestCounter: AtomicInteger = AtomicInteger(0)
    private val totalUrlsCounter: AtomicInteger = AtomicInteger(0)
    private val processedUrlCounter: AtomicInteger = AtomicInteger(0)
    private val currentLevel: AtomicInteger = AtomicInteger(0)
    private val urlsByLevel: MutableMap<Int, MutableList<String>> =
        mutableMapOf<Int, MutableList<String>>().withDefault { mutableListOf() }

    init {
        val urls = urlsByLevel.getValue(currentLevel.get())
        urls.add(request.url)
        urlsByLevel[currentLevel.get()] = urls
    }

    fun getCurrentLevelUrls(): MutableList<String> {
        return urlsByLevel.getValue(currentLevel.get())
    }

    fun getNextLevelUrls(): MutableList<String> {
        val nextLevel = currentLevel.get() + 1
        return urlsByLevel.getValue(nextLevel)
    }

    fun addNextLevelUrls(parentUrls: List<String>) {
        val nexLevel = currentLevel.get() + 1
        val urlOnNextLevel = urlsByLevel.getValue(nexLevel)
        urlOnNextLevel.addAll(parentUrls)
        urlsByLevel[nexLevel] = urlOnNextLevel

        processedUrlCounter.incrementAndGet()
        totalUrlsCounter.incrementAndGet()
        totalUrlsCounter.addAndGet(parentUrls.size - 1)
    }

    fun getLevel() = currentLevel.get()

    fun isSendRequestEnable(): Boolean = requestCounter.get() < maxUrlsCount

    fun incRequest() = requestCounter.incrementAndGet()

    fun isSearchCompleted(): Boolean {
        return processedUrlCounter.get() >= maxUrlsCount || totalUrlsCounter.get() == processedUrlCounter.get()
    }

    fun incLevel() = currentLevel.incrementAndGet()

    fun clean() {
        processedUrlCounter.set(0)
        requestCounter.set(0)
        totalUrlsCounter.set(0)
        currentLevel.set(0)
        urlsByLevel.clear()
    }
}