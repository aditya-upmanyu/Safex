package com.safex.app.util

object PhoneNumberValidator {
    
    private val INDIAN_PHONE_REGEX = Regex("^[+]?91?[6-9]\\d{9}$")

    fun isValidIndianPhoneNumber(phoneNumber: String): Boolean {
        val cleaned = phoneNumber.replace("\\s".toRegex(), "")
        return INDIAN_PHONE_REGEX.matches(cleaned)
    }

    fun formatPhoneNumber(phoneNumber: String): String {
        val cleaned = phoneNumber.replace("\\s".toRegex(), "")
        return when {
            cleaned.startsWith("+91") -> cleaned
            cleaned.startsWith("91") && cleaned.length == 12 -> "+$cleaned"
            cleaned.length == 10 -> "+91$cleaned"
            else -> cleaned
        }
    }
}
