package com.bohdan.khristov.textsearch.ui.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

abstract class BaseViewModel : ViewModel() {

    internal val jobs = mutableListOf<Job>()

    override fun onCleared() {
        jobs.clear()
        super.onCleared()
    }
}