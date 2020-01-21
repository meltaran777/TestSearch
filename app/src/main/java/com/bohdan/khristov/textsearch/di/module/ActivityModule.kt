package com.bohdan.khristov.textsearch.di.module

import com.bohdan.khristov.textsearch.ui.screen.search.SearchActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): SearchActivity
}