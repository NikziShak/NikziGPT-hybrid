package com.nikzigpt

import android.app.Application
import androidx.room.Room
import com.nikzigpt.repository.ChatDatabase

class NikziGPTApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        ChatDatabase.getDatabase(this)
    }
}