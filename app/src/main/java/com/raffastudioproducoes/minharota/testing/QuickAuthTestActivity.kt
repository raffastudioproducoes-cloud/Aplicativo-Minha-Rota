package com.raffastudioproducoes.minharota.testing

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class QuickAuthTestActivity : AppCompatActivity() {
    private val TAG = "QUICK_AUTH_TEST"
    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logEvent("═══════════════════════════════════════════════════════")
        logEvent("🧪 QUICK AUTH TEST — Firebase Auth Emulator Validation")
        logEvent("═══════════════════════════════════════════════════════")

        val testEmail = "quicktest-${UUID.randomUUID().toString().take(8)}@test.local"
        val testPassword = "Test@12345"

        logEvent("\n[TEST] Creating account: $testEmail")
        logEvent("Waiting for auth response...")

        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnSuccessListener { result ->
                logEvent("✅ Account created successfully!")
                logEvent("   UID: ${result.user?.uid}")
                logEvent("   Email: ${result.user?.email}")
                finish()
            }
            .addOnFailureListener { e ->
                logEvent("❌ Account creation failed!")
                logEvent("   Error: ${e.message}")
                logEvent("   Exception: ${e::class.simpleName}")
                finish()
            }

        // Safety timeout after 30 seconds
        Thread {
            Thread.sleep(30000)
            if (!isFinishing) {
                logEvent("❌ TEST TIMEOUT AFTER 30 SECONDS")
                finish()
            }
        }.start()
    }

    private fun logEvent(message: String) {
        val timestamp = dateFormat.format(Date())
        Log.i(TAG, "[$timestamp] $message")
    }
}
