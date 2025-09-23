package com.company.secureapp

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context): Context {
        // Применяем сохраненный язык к контексту
        return updateBaseContextLocale(super.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Обновляем язык для активити
        updateActivityLocale()
    }

    // ОБНОВЛЯЕМ ЛОКАЛЬ ДЛЯ АКТИВНОСТИ
    private fun updateActivityLocale() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    // ОБНОВЛЯЕМ КОНТЕКСТ С ЛОКАЛЬЮ
    private fun updateBaseContextLocale(context: Context): Context {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            context
        }
    }

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    // МЕТОД ДЛЯ СМЕНЫ ЯЗЫКА (ДЛЯ СОВМЕСТИМОСТИ)
    protected fun changeLanguage(languageCode: String) {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("selected_language", languageCode).apply()
        
        // Применяем язык через Application
        (application as? SecureApp)?.setAppLocale(languageCode)
        
        // Перезапускаем активити
        recreate()
    }
}
