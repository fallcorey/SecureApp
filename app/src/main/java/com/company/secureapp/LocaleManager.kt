package com.company.secureapp

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, language: String): Context {
        return updateResources(context, language)
    }

    fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        return context
    }

    fun getCurrentLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return preferences.getString("selected_language", "en") ?: "en"
    }

    fun saveLanguage(context: Context, language: String) {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        preferences.edit().putString("selected_language", language).apply()
    }
}
