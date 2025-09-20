package com.company.secureapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
        
        // Полностью перезапускаем приложение для применения языка
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
    }

    // Вспомогательные методы для Toast
    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun showToast(stringResId: Int) {
        Toast.makeText(this, getString(stringResId), Toast.LENGTH_LONG).show()
    }
}
