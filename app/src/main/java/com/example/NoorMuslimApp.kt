package com.example

import android.app.Application
import com.example.core.util.AppContainer
import com.example.core.util.DefaultAppContainer

class NoorMuslimApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
