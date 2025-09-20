package com.company.secureapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fallcorey.secureapp.utils.LocaleManager

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase, LocaleManager.getCurrentLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Применяем язык сразу при создании активности
        LocaleManager.setLocale(this, LocaleManager.getCurrentLanguage(this))
    }

    fun changeLanguage(languageCode: String) {
        LocaleManager.saveLanguage(this, languageCode)
        LocaleManager.setLocale(this, languageCode)
        
        // Пересоздаем активность для применения изменений
        recreate()
    }
}
