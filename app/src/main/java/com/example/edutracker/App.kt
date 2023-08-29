package com.example.edutracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("ar")
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}