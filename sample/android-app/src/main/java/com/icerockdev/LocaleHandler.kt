package com.icerockdev

import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.preference.PreferenceManager
import java.util.*

object LocaleHandler {

    const val LOCALE_PREF_TAG = "pref:locale"

    fun updateLocale(newBase: Context): Context {
        val currentLocale = PreferenceManager.getDefaultSharedPreferences(newBase)
            .getString(LOCALE_PREF_TAG, Locale.getDefault().language)

        val newLocale = Locale(currentLocale)
        Locale.setDefault(newLocale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newBase.resources.configuration.locales = LocaleList(newLocale)
            newBase.createConfigurationContext(newBase.resources.configuration)
        } else {
            newBase.resources.configuration.locale = newLocale
            newBase.resources.updateConfiguration(newBase.resources.configuration, newBase.resources.displayMetrics)
            newBase
        }
    }

}