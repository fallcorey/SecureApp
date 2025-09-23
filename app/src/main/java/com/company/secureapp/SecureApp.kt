package com.company.secureapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import java.util.*

class SecureApp : Application() {

    companion object {
        private const val TAG = "SecureApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        
        // Восстанавливаем сохраненный язык при запуске приложения
        restoreSavedLocale()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
        
        // При изменении конфигурации (например, поворот экрана) сохраняем язык
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
        setAppLocale(languageCode)
    }

    /**
     * Восстанавливает сохраненный язык из SharedPreferences
     */
    private fun restoreSavedLocale() {
        try {
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
            
            Log.d(TAG, "Restoring saved locale: $languageCode")
            setAppLocale(languageCode)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring locale: ${e.message}")
            // Устанавливаем язык по умолчанию в случае ошибки
            setAppLocale("en")
        }
    }

    /**
     * Устанавливает язык для всего приложения
     * @param languageCode код языка (например, "en", "ru")
     */
    fun setAppLocale(languageCode: String) {
        try {
            Log.d(TAG, "Setting app locale to: $languageCode")
            
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            
            val resources = resources
            val configuration = Configuration(resources.configuration)
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                // Создаем новый контекст с обновленной конфигурацией
                val context = createConfigurationContext(configuration)
                resources.updateConfiguration(configuration, resources.displayMetrics)
                
                // Обновляем ресурсы основного контекста приложения
                super.getResources().updateConfiguration(configuration, resources.displayMetrics)
            } else {
                @Suppress("DEPRECATION")
                configuration.locale = locale
                @Suppress("DEPRECATION")
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            
            // Сохраняем выбор языка
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("selected_language", languageCode).apply()
            
            Log.d(TAG, "Locale set successfully: $languageCode")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting app locale: ${e.message}")
        }
    }

    /**
     * Получает текущий язык приложения
     */
    fun getCurrentLanguage(): String {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("selected_language", "en") ?: "en"
    }

    /**
     * Переопределяем получение ресурсов для корректной работы с локализацией
     */
    override fun getResources(): Resources {
        var resources = super.getResources()
        try {
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
            val locale = Locale(languageCode)
            
            val configuration = Configuration(resources.configuration)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val context = createConfigurationContext(configuration)
                resources = context.resources
            } else {
                @Suppress("DEPRECATION")
                configuration.locale = locale
                @Suppress("DEPRECATION")
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getResources: ${e.message}")
        }
        return resources
    }

    /**
     * Переопределяем получение контекста для корректной работы с локализацией
     */
    override fun getApplicationContext(): Context {
        var context = super.getApplicationContext()
        try {
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val languageCode = sharedPreferences.getString("selected_language", "en") ?: "en"
            val locale = Locale(languageCode)
            
            val configuration = Configuration(context.resources.configuration)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                context = super.getApplicationContext().createConfigurationContext(configuration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getApplicationContext: ${e.message}")
        }
        return context
    }
}
