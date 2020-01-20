package com.bohdan.khristov.textsearch.ui.screen.home

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.ui.screen.home.fragment.TabsFragment
import com.careclix.doctor.ui.common.BaseActivity

class HomeActivity : BaseActivity() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel::class.java)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, TabsFragment.newInstance())
            .commit()
    }
}
