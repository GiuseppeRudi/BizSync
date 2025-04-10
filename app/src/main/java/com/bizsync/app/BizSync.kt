package com.bizsync.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BizSync : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inizializzazioni globali, se necessarie
    }
}
