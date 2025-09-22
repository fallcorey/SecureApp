package com.company.secureapp

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase))
    }

    private fun updateLocale(context: Context): Context {
        val preferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val language = preferences.getString("selected_language", "en") ?: "en"
        return setLocale(context, language)
    }

    private fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Применяем язык при создании активности
        applyLanguage()
    }

    private fun applyLanguage() {
        val preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val language = preferences.getString("selected_language", "en") ?: "en"
        
        val resources: Resources = resources
        val configuration: Configuration = resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun changeLanguage(languageCode: String) {
        // Сохраняем выбранный язык
        val preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        preferences.edit().putString("selected_language", languageCode).apply()
        
        // Немедленно применяем изменения языка
        applyLanguage()
        
        // Обновляем контент активности
        recreate()
    }

    // Вспомогательные методы для Toast
    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun showToast(stringResId: Int) {
        Toast.makeText(this, getString(stringResId), Toast.LENGTH_LONG).show()
    }

    protected fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun showError(stringResId: Int) {
        Toast.makeText(this, getString(stringResId), Toast.LENGTH_LONG).show()
    }
}
