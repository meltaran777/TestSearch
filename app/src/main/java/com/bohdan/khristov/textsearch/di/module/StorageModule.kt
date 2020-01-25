package com.bohdan.khristov.textsearch.di.module

import com.bohdan.khristov.textsearch.data.repository.ISearchRepository
import com.bohdan.khristov.textsearch.data.repository.SearchRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun provideSearchRepository(): ISearchRepository =
        SearchRepository()
}