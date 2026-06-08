package com.example.vibecodechatter.data.local

import android.content.Context

class UserPreferences(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastUsername(): String {
        return sharedPreferences.getString(KEY_LAST_USERNAME, "")?.trim().orEmpty()
    }

    fun saveLastUsername(username: String) {
        sharedPreferences.edit()
            .putString(KEY_LAST_USERNAME, username.trim())
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "vibe_code_chatter_prefs"
        const val KEY_LAST_USERNAME = "last_username"
    }
}

