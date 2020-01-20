package com.bohdan.khrystov.textsearchdi

import android.app.Application
import com.bohdan.khrystov.textsearch.App
import com.bohdan.khristov.textsearch.di.module.ActivityModule
import com.bohdan.khristov.textsearch.di.module.ApplicationModule
import com.bohdan.khristov.textsearch.di.module.ServiceBuilderModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ApplicationModule::class,
    ActivityModule::class,
    ServiceBuilderModule::class
])
interface ApplicationComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }

    fun inject(app: App)
}