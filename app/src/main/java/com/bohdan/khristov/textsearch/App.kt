package com.bohdan.khrystov.textsearch

import android.app.Activity
import android.app.Application
import android.app.Service
import android.os.StrictMode
import com.bohdan.khrystov.textsearchdi.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
    }

    override fun activityInjector() = dispatchingAndroidInjector

    override fun serviceInjector(): AndroidInjector<Service> = dispatchingServiceInjector
}
