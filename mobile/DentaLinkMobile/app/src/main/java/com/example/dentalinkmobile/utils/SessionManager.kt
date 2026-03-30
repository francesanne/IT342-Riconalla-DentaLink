package com.example.dentalinkmobile.utils

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("DentaLinkSession", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}