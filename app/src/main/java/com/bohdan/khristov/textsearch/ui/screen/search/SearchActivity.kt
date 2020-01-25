package com.bohdan.khristov.textsearch.ui.screen.search

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.bohdan.khristov.textsearch.R
import com.bohdan.khristov.textsearch.ui.screen.search.fragment.TabsFragment
import com.careclix.doctor.ui.common.BaseActivity

class SearchActivity : BaseActivity() {

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchViewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, TabsFragment.newInstance())
            .commit()
    }
}
