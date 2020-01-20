package com.bohdan.khristov.textsearch.di.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [
    StorageModule::class,
    ViewModelModule::class
])
class ApplicationModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context {
        return app
    }
}