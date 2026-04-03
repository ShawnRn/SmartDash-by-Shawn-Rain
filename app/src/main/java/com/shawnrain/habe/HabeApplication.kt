package com.shawnrain.habe

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class HabeApplication : Application(), ViewModelStoreOwner {
    private val appViewModelStore = ViewModelStore()

    val appViewModelFactory: ViewModelProvider.Factory by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}
