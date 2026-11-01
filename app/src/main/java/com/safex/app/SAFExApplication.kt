package com.safex.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SAFExApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load native SQLCipher library for 16 KB page-size support
        System.loadLibrary("sqlcipher")
    }
}
