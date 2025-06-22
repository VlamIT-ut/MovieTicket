// UserPrefs.kt
package com.example.movieticket.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var point: Int
        get() = prefs.getInt("point", 0)
        set(value) = prefs.edit().putInt("point", value).apply()

    var memberLevel: String
        get() = prefs.getString("level", "Silver") ?: "Silver"
        set(value) = prefs.edit().putString("level", value).apply()
}
