package com.raffastudioproducoes.minharota.testing

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RealUsageTestActivity : AppCompatActivity() {

    private val TAG = "REAL_USAGE_TEST"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val logs = mutableListOf<String>()

    private var testsPassed = 0
    private var testsFailed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logEvent("═══════════════════════════════════════════════════════")
        logEvent("🧪 REAL USAGE TEST — Starting Complete App Flow")
        logEvent("═══════════════════════════════════════════════════════")

        runCompleteUserFlow()
    }

    private fun runCompleteUserFlow() {
        val testEmail = "realuser-${UUID.randomUUID().toString().take(8)}@test.local"
        val testPassword = "Test@12345"

        logEvent("\n[PHASE 1] AUTHENTICATION")
        logEvent("────────────────────────────────────────────────────────")
        logEvent("Creating account: $testEmail")

        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                logEvent("✅ [1.1] Account created")
                logEvent("   UID: $uid")
                logEvent("   Email: $testEmail")
                testsPassed++

                logEvent("\n[PHASE 2] CREATE TURNO (Insert)")
                logEvent("────────────────────────────────────────────────────────")
                createTurnoPhase(uid, testEmail)
            }
            .addOnFailureListener { e ->
                logEvent("❌ [1.0] Account creation failed: ${e.message}")
                testsFailed++
                finalizeAndReport()
            }
    }

    private fun createTurnoPhase(uid: String, email: String) {
        val turnoId = "turno-real-${UUID.randomUUID().toString().take(8)}"
        val turnoData = mapOf(
            "id" to turnoId,
            "data" to "07/09/2024",
            "horaInicio" to "08:00",
            "horaFim" to "17:00",
            "houvePausa" to true,
            "horaInicioPausa" to "12:00",
            "horaFimPausa" to "13:00",
            "ganhoBruto" to 250.50,
            "custoRua" to 50.00,
            "ganhoLiquido" to 200.50,
            "corridas" to listOf(
                mapOf("id" to "ride_1", "valor" to 50.00, "km" to 10.0, "app" to "Uber"),
                mapOf("id" to "ride_2", "valor" to 75.00, "km" to 15.0, "app" to "99Taxi"),
                mapOf("id" to "ride_3", "valor" to 75.50, "km" to 12.0, "app" to "Uber")
            ),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        logEvent("Creating turno:")
        logEvent("  Date: 07/09/2024")
        logEvent("  Time: 08:00 - 17:00 (with pause 12:00-13:00)")
        logEvent("  Revenue: R$ 250.50 (gross) → R$ 200.50 (net)")
        logEvent("  Rides: 3 (R$ 50, R$ 75, R$ 75.50)")

        db.collection("usuarios").document(uid).collection("turnos")
            .document(turnoId).set(turnoData)
            .addOnSuccessListener {
                logEvent("✅ [2.1] Turno created successfully")
                logEvent("   ID: $turnoId")
                testsPassed++

                logEvent("\n[PHASE 3] READ TURNO (View Data)")
                logEvent("────────────────────────────────────────────────────────")
                readTurnoPhase(uid, turnoId)
            }
            .addOnFailureListener { e ->
                logEvent("❌ [2.0] Turno creation failed: ${e.message}")
                testsFailed++
                updateAndDeletePhase(uid, "turno-null", email)
            }
    }

    private fun readTurnoPhase(uid: String, turnoId: String) {
        db.collection("usuarios").document(uid).collection("turnos")
            .document(turnoId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val data = doc.data
                    logEvent("✅ [3.1] Turno retrieved successfully")
                    logEvent("  Data: ${data?.get("data")}")
                    logEvent("  Gross: R$ ${data?.get("ganhoBruto")}")
                    logEvent("  Net: R$ ${data?.get("ganhoLiquido")}")
                    logEvent("  Rides: ${(data?.get("corridas") as? List<*>)?.size}")
                    testsPassed++
                } else {
                    logEvent("❌ [3.0] Turno not found")
                    testsFailed++
                }

                logEvent("\n[PHASE 4] UPDATE TURNO (Edit Data)")
                logEvent("────────────────────────────────────────────────────────")
                updateAndDeletePhase(uid, turnoId, "")
            }
            .addOnFailureListener { e ->
                logEvent("❌ [3.0] Read failed: ${e.message}")
                testsFailed++
                updateAndDeletePhase(uid, turnoId, "")
            }
    }

    private fun updateAndDeletePhase(uid: String, turnoId: String, email: String) {
        if (turnoId == "turno-null") {
            logEvent("⚠️  [4.0] Skipping update (no turno created)")
            testsFailed++
            deleteTestDataPhase(uid, email)
            return
        }

        val updateData = mapOf(
            "ganhoBruto" to 300.00,
            "ganhoLiquido" to 250.00,
            "updatedAt" to System.currentTimeMillis()
        )

        logEvent("Updating turno:")
        logEvent("  New Gross: R$ 300.00")
        logEvent("  New Net: R$ 250.00")

        db.collection("usuarios").document(uid).collection("turnos")
            .document(turnoId).update(updateData)
            .addOnSuccessListener {
                logEvent("✅ [4.1] Turno updated successfully")
                testsPassed++

                logEvent("\n[PHASE 5] DELETE TURNO (Remove Data)")
                logEvent("────────────────────────────────────────────────────────")
                logEvent("Deleting turno: $turnoId")

                db.collection("usuarios").document(uid).collection("turnos")
                    .document(turnoId).delete()
                    .addOnSuccessListener {
                        logEvent("✅ [5.1] Turno deleted successfully")
                        testsPassed++

                        logEvent("\n[PHASE 6] VERIFY DELETION")
                        logEvent("────────────────────────────────────────────────────────")
                        db.collection("usuarios").document(uid).collection("turnos")
                            .document(turnoId).get()
                            .addOnSuccessListener { doc ->
                                if (!doc.exists()) {
                                    logEvent("✅ [6.1] Turno confirmed deleted")
                                    testsPassed++
                                } else {
                                    logEvent("❌ [6.0] Turno still exists after delete")
                                    testsFailed++
                                }
                                deleteTestDataPhase(uid, email)
                            }
                            .addOnFailureListener { e ->
                                logEvent("❌ [6.0] Verification failed: ${e.message}")
                                testsFailed++
                                deleteTestDataPhase(uid, email)
                            }
                    }
                    .addOnFailureListener { e ->
                        logEvent("❌ [5.0] Delete failed: ${e.message}")
                        testsFailed++
                        deleteTestDataPhase(uid, email)
                    }
            }
            .addOnFailureListener { e ->
                logEvent("❌ [4.0] Update failed: ${e.message}")
                testsFailed++
                deleteTestDataPhase(uid, email)
            }
    }

    private fun deleteTestDataPhase(uid: String, email: String) {
        logEvent("\n[PHASE 7] CLEANUP")
        logEvent("────────────────────────────────────────────────────────")
        logEvent("Deleting test user: $uid")

        auth.currentUser?.delete()
            ?.addOnSuccessListener {
                logEvent("✅ [7.1] Test user deleted")
                logEvent("   Email: $email")
                finalizeAndReport()
            }
            ?.addOnFailureListener { e ->
                logEvent("⚠️  [7.0] Failed to delete user: ${e.message}")
                finalizeAndReport()
            }
    }

    private fun finalizeAndReport() {
        Thread.sleep(2000)

        logEvent("\n═══════════════════════════════════════════════════════")
        logEvent("📊 TEST RESULTS")
        logEvent("═══════════════════════════════════════════════════════")
        logEvent("\n✅ PASSED: $testsPassed")
        logEvent("❌ FAILED: $testsFailed")
        logEvent("📊 TOTAL:  ${testsPassed + testsFailed}\n")

        if (testsFailed == 0) {
            logEvent("🎉 ALL REAL USAGE TESTS PASSED!")
        } else {
            logEvent("⚠️  ${testsFailed} test(s) failed. Check logs above.")
        }

        logEvent("═══════════════════════════════════════════════════════")

        // Dump all logs
        Log.i(TAG, "\n\n========== COMPLETE TEST LOG ==========")
        logs.forEach { log ->
            Log.i(TAG, log)
        }
        Log.i(TAG, "================== END LOG ====================\n")

        finish()
    }

    private fun logEvent(message: String) {
        val timestamp = dateFormat.format(Date())
        val logLine = "[$timestamp] $message"
        logs.add(logLine)
        Log.i(TAG, logLine)
    }
}
