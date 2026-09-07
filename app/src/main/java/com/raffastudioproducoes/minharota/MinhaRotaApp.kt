package com.raffastudioproducoes.minharota

import android.app.Application
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MinhaRotaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val providerFactory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(providerFactory)

        // Conectar ao Firebase Emulator em DEBUG
        if (BuildConfig.DEBUG) {
            try {
                // Desabilitar App Check para emulador (senão bloqueia requisições)
                FirebaseAppCheck.getInstance().getAppCheckToken(false)
                    .addOnCompleteListener {
                        // Token obtido sem validação rigorosa em DEBUG
                        Log.d("MinhaRotaApp", "App Check token obtained for emulator")
                    }

                // Conectar Firestore ao emulador (10.0.2.2 = host do emulator Android)
                val firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setHost("10.0.2.2:8180")
                    .setSslEnabled(false)
                    .setPersistenceEnabled(false)
                    .build()
                FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings
                Log.d("MinhaRotaApp", "✅ Firestore connected to emulator at 10.0.2.2:8180")

                // Conectar Auth ao emulador
                FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9199)
                Log.d("MinhaRotaApp", "✅ Auth connected to emulator at 10.0.2.2:9199")

            } catch (e: Exception) {
                Log.e("MinhaRotaApp", "❌ Emulator connection error: ${e.message}", e)
                // Continuar com Firebase normal se emulador falhar
            }
        }

        // Inicializa apenas o CrashHandler conforme solicitado na Fase 1
        CrashHandler(this)
    }
}
