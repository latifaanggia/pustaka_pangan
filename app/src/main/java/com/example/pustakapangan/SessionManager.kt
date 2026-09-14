package com.example.pustakapangan

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "session_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_LOGGED_IN, false)

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun logout(context: Context) = setLoggedIn(context, false)
}