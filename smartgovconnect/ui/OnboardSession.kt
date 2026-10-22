package com.example.smartgovconnect.util

import android.content.Context

object OnboardingSession {
    private const val PREFS_NAME = "smartgov_onboarding"
    private const val KEY_COMPLETED = "onboarding_completed"

    fun isCompleted(context: Context): Boolean {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getBoolean(KEY_COMPLETED, false)
    }

    fun setCompleted(context: Context) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean(KEY_COMPLETED, true)
            .apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean(KEY_COMPLETED, false)
            .apply()
    }
}