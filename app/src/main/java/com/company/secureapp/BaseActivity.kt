package com.company.secureapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val localeManager = com.company.secureapp.utils.LocaleManager
        super.attachBaseContext(localeManager.setLocale(newBase, localeManager.getCurrentLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Применяем язык сразу при создании активности
        com.company.secureapp.utils.LocaleManager.setLocale(this, 
            com.company.secureapp.utils.LocaleManager.getCurrentLanguage(this))
    }

    fun changeLanguage(languageCode: String) {
        com.company.secureapp.utils.LocaleManager.saveLanguage(this, languageCode)
        com.company.secureapp.utils.LocaleManager.setLocale(this, languageCode)
        
        // Пересоздаем активность для применения изменений
        recreate()
    }
}
