package com.bohdan.khristov.textsearch.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

inline fun <FRAGMENT : Fragment> FRAGMENT.putArgs(argsBuilder: Bundle.() -> Unit): FRAGMENT = this.apply { arguments = Bundle().apply(argsBuilder) }

abstract class BaseFragment : Fragment() {

    abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    fun setToolbarTitle(title: String) {
        (activity as AppCompatActivity).supportActionBar
                ?.setTitle(title)
    }
}