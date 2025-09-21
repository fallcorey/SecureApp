package com.company.secureapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Вспомогательные методы для Toast
    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun showToast(stringResId: Int) {
        Toast.makeText(this, getString(stringResId), Toast.LENGTH_LONG).show()
    }

    // Добавлен метод showError для совместимости
    protected fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun showError(stringResId: Int) {
        Toast.makeText(this, getString(stringResId), Toast.LENGTH_LONG).show()
    }
}
