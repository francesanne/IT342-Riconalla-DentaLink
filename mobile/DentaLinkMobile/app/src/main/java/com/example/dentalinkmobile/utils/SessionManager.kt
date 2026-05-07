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

    /** Persist role (ADMIN or PATIENT) and display name for routing and welcome UI. */
    fun saveUserInfo(role: String, firstName: String) {
        prefs.edit()
            .putString("role", role)
            .putString("firstName", firstName)
            .apply()
    }

    fun getRole(): String? {
        return prefs.getString("role", null)
    }

    fun getFirstName(): String? {
        return prefs.getString("firstName", null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}