package com.company.secureapp

import android.content.Context
import android.content.SharedPreferences

class SimplePreferenceHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Сохранение строки
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
        println("Saved: $key = $value") // Для отладки
    }

    // Получение строки
    fun getString(key: String, defaultValue: String = ""): String {
        val value = sharedPreferences.getString(key, defaultValue) ?: defaultValue
        println("Loaded: $key = $value") // Для отладки
        return value
    }

    // Сохранение boolean
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    // Получение boolean
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    // Очистка всех настроек
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
