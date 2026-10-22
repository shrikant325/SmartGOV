package com.example.smartgovconnect.util

import android.content.Context

/**
 * Regular users stay logged in automatically via Firebase Auth's own persisted
 * session (SplashActivity just checks FirebaseAuth.currentUser). The admin login
 * is a separate, hardcoded-credentials flow that never touches Firebase Auth —
 * so it needs its OWN simple "remember me" mechanism. This is that mechanism:
 * one boolean flag saved to SharedPreferences.
 */
object AdminSession {
    private const val PREFS_NAME = "smartgov_prefs"
    private const val KEY_ADMIN_LOGGED_IN = "admin_logged_in"

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ADMIN_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ADMIN_LOGGED_IN, false)
    }
}