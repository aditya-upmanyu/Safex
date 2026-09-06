package com.safex.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SAFExApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
