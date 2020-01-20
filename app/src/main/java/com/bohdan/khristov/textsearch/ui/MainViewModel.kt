package com.bohdan.khristov.textsearch.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bohdan.khristov.textsearch.domain.SearchInteractor
import com.bohdan.khristov.textsearch.domain.common.Resource.Status.*
import javax.inject.Inject

class MainViewModel @Inject constructor(private val searchInteractor: SearchInteractor) : ViewModel() {

    var url = ""

    val text = MutableLiveData<String>()

    fun search() {
        searchInteractor.search(url) {
            when (it.status) {
                SUCCESS -> {
                    it.data?.let { fetchedText ->
                        text.postValue(fetchedText)
                    }
                }
                ERROR -> {}
                LOADING -> {}
            }
        }
    }
}