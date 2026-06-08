package com.example.vibecodechatter

import android.app.Application
import com.example.vibecodechatter.data.local.UserPreferences
import com.example.vibecodechatter.data.repository.MessageRepository

class ChatterApplication : Application() {
    val messageRepository: MessageRepository by lazy {
        MessageRepository()
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(applicationContext)
    }
}

