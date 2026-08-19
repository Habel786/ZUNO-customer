package com.example

import android.app.Application
import android.util.Log
import com.example.data.firebase.FirebaseConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ZunoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            val requiredOptions = FirebaseOptions.Builder()
                .setApiKey(FirebaseConfig.API_KEY)
                .setApplicationId(FirebaseConfig.APP_ID)
                .setProjectId(FirebaseConfig.PROJECT_ID)
                .setStorageBucket(FirebaseConfig.STORAGE_BUCKET)
                .setGcmSenderId(FirebaseConfig.MESSAGING_SENDER_ID)
                .build()

            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(this, requiredOptions)
                Log.d("ZunoApplication", "FirebaseApp initialized with project: ${FirebaseConfig.PROJECT_ID}")
            } else {
                val currentApp = FirebaseApp.getInstance()
                if (currentApp.options.apiKey != FirebaseConfig.API_KEY || currentApp.options.projectId != FirebaseConfig.PROJECT_ID) {
                    Log.w("ZunoApplication", "Updating FirebaseApp with verified configuration")
                    currentApp.delete()
                    FirebaseApp.initializeApp(this, requiredOptions)
                } else {
                    Log.d("ZunoApplication", "FirebaseApp verified for ${FirebaseConfig.PROJECT_ID}")
                }
            }
        } catch (e: Exception) {
            Log.e("ZunoApplication", "Failed to initialize FirebaseApp: ${e.message}", e)
        }
    }
}
