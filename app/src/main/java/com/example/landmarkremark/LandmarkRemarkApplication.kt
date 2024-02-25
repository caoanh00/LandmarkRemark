package com.example.landmarkremark

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import timber.log.Timber

class LandmarkRemarkApplication : Application() {

    /**
     * Application context.
     */
    companion object {
        lateinit var application: Application

        //an universal context across the app
        fun getContext(): Context {
            return application.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        //init Firebase
        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())
    }
}