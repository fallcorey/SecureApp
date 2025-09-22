package com.company.secureapp

// Data class для результата отправки
data class AlertResult(
    val success: Boolean,
    val messages: List<String>,
    val details: String
)
