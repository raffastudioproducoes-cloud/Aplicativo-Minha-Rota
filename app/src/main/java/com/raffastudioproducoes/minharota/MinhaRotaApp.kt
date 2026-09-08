package com.raffastudioproducoes.minharota

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MinhaRotaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase production (sem emulator)
        CrashHandler(this)
    }
}
