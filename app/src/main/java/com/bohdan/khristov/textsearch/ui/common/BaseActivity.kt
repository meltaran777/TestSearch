package com.careclix.doctor.ui.common

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.bohdan.khristov.textsearch.R
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), HasActivityInjector {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    protected lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

/*    protected fun <T : ViewDataBinding> provideBinding(layoutId: Int): T {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    protected fun setupNavigateBackToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(android.R.drawable.ar)
        setSupportActionBar(toolbar)
    }*/
}