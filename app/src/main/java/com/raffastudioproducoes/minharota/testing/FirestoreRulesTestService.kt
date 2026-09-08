package com.raffastudioproducoes.minharota.testing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class FirestoreRulesTestService : Service() {

    private val TAG = "FIRESTORE_TEST"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var testsPassed = 0
    private var testsFailed = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "═══════════════════════════════════════════════════")
        Log.i(TAG, "FIRESTORE RULES TESTS — Starting (Service)")
        Log.i(TAG, "═══════════════════════════════════════════════════")

        createTestUserAndRunTests()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createTestUserAndRunTests() {
        val testEmail = "test-${UUID.randomUUID().toString().take(8)}@test.local"
        val testPassword = "Test@12345"

        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                Log.i(TAG, "\n✅ Test user created: $uid")
                Log.i(TAG, "   Email: $testEmail")

                runAllTests(uid)
                cleanupAndReport(uid)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to create test user: ${e.message}", e)
                stopSelf()
            }
    }

    private fun runAllTests(uid: String) {
        Log.i(TAG, "\n------- TEST SUITE -------\n")

        test1_CreateValidTurno(uid)
        test2_CreateTurnoWithoutId(uid)
        test3_UpdateUsuarioBlockPro(uid)
        test4_ReadAnotherUsersData(uid)
        test5_CreateWithoutAuth()
        test6_DeleteOwnTurno(uid)
    }

    private fun test1_CreateValidTurno(uid: String) {
        Log.i(TAG, "\n[TEST 1] CREATE Turno with valid data")

        val turnoId = "turno-${UUID.randomUUID().toString().take(8)}"
        val turnoData = mapOf(
            "id" to turnoId,
            "data" to "07/09/2024",
            "horaInicio" to "08:00",
            "horaFim" to "17:00",
            "houvePausa" to true,
            "horaInicioPausa" to "12:00",
            "horaFimPausa" to "13:00",
            "ganhoBruto" to 150.50,
            "custoRua" to 25.00,
            "ganhoLiquido" to 125.50,
            "corridas" to emptyList<Any>(),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("usuarios").document(uid).collection("turnos").document(turnoId).set(turnoData)
            .addOnSuccessListener {
                Log.i(TAG, "  ✅ PASS — Turno criado com sucesso")
                testsPassed++
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ❌ FAIL — ${e.message}")
                testsFailed++
            }
    }

    private fun test2_CreateTurnoWithoutId(uid: String) {
        Log.i(TAG, "\n[TEST 2] CREATE Turno WITHOUT id field (should DENY)")

        val turnoId = "turno-${UUID.randomUUID().toString().take(8)}"
        val turnoData = mapOf(
            "data" to "07/09/2024",
            "horaInicio" to "08:00",
            "ganhoBruto" to 150.50,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("usuarios").document(uid).collection("turnos").document(turnoId).set(turnoData)
            .addOnSuccessListener {
                Log.e(TAG, "  ❌ FAIL — Should have been DENIED (missing id)")
                testsFailed++
            }
            .addOnFailureListener { e ->
                if ("PERMISSION_DENIED" in e.message.orEmpty()) {
                    Log.i(TAG, "  ✅ PASS — Correctly DENIED: ${e.message}")
                    testsPassed++
                } else {
                    Log.e(TAG, "  ❌ FAIL — Wrong error: ${e.message}")
                    testsFailed++
                }
            }
    }

    private fun test3_UpdateUsuarioBlockPro(uid: String) {
        Log.i(TAG, "\n[TEST 3] UPDATE Usuario trying to modify isPro (should DENY)")

        val userData = mapOf(
            "nome" to "Test User",
            "email" to "test@example.com",
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("usuarios").document(uid).set(userData)
            .addOnSuccessListener {
                db.collection("usuarios").document(uid).update(mapOf(
                    "isPro" to true,
                    "nome" to "Updated Name"
                ))
                    .addOnSuccessListener {
                        Log.e(TAG, "  ❌ FAIL — Should have blocked isPro update")
                        testsFailed++
                    }
                    .addOnFailureListener { e ->
                        if ("PERMISSION_DENIED" in e.message.orEmpty()) {
                            Log.i(TAG, "  ✅ PASS — Correctly blocked: ${e.message}")
                            testsPassed++
                        } else {
                            Log.e(TAG, "  ❌ FAIL — Wrong error: ${e.message}")
                            testsFailed++
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ❌ FAIL — Could not create usuario doc: ${e.message}")
                testsFailed++
            }
    }

    private fun test4_ReadAnotherUsersData(uid: String) {
        Log.i(TAG, "\n[TEST 4] READ another user's data (should DENY)")

        val otherUserId = "another-user-${UUID.randomUUID().toString().take(6)}"

        db.collection("usuarios").document(otherUserId).collection("turnos").document("some-turno").get()
            .addOnSuccessListener {
                Log.e(TAG, "  ❌ FAIL — Should have been DENIED")
                testsFailed++
            }
            .addOnFailureListener { e ->
                if ("PERMISSION_DENIED" in e.message.orEmpty()) {
                    Log.i(TAG, "  ✅ PASS — Correctly DENIED: ${e.message}")
                    testsPassed++
                } else {
                    Log.e(TAG, "  ❌ FAIL — Wrong error: ${e.message}")
                    testsFailed++
                }
            }
    }

    private fun test5_CreateWithoutAuth() {
        Log.i(TAG, "\n[TEST 5] CREATE without Authentication (should DENY)")

        val currentUid = auth.currentUser?.uid ?: return

        auth.signOut()

        val userData = mapOf(
            "nome" to "Unauthorized",
            "email" to "unauth@example.com",
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("usuarios").document(currentUid).set(userData)
            .addOnSuccessListener {
                Log.e(TAG, "  ❌ FAIL — Should have been DENIED (no auth)")
                testsFailed++
                auth.signInWithEmailAndPassword("test@example.com", "Test@12345").addOnCompleteListener { }
            }
            .addOnFailureListener { e ->
                if ("PERMISSION_DENIED" in e.message.orEmpty() || "UNAUTHENTICATED" in e.message.orEmpty()) {
                    Log.i(TAG, "  ✅ PASS — Correctly DENIED: ${e.message}")
                    testsPassed++
                } else {
                    Log.e(TAG, "  ❌ FAIL — Wrong error: ${e.message}")
                    testsFailed++
                }
                auth.signInWithEmailAndPassword("test@example.com", "Test@12345").addOnCompleteListener { }
            }
    }

    private fun test6_DeleteOwnTurno(uid: String) {
        Log.i(TAG, "\n[TEST 6] DELETE own Turno (should ALLOW)")

        val turnoId = "turno-to-delete-${UUID.randomUUID().toString().take(8)}"

        val turnoData = mapOf(
            "id" to turnoId,
            "data" to "07/09/2024",
            "horaInicio" to "08:00",
            "horaFim" to "17:00",
            "ganhoBruto" to 100.0,
            "custoRua" to 10.0,
            "ganhoLiquido" to 90.0,
            "houvePausa" to false,
            "corridas" to emptyList<Any>(),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("usuarios").document(uid).collection("turnos").document(turnoId).set(turnoData)
            .addOnSuccessListener {
                db.collection("usuarios").document(uid).collection("turnos").document(turnoId).delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "  ✅ PASS — Turno deletado com sucesso")
                        testsPassed++
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "  ❌ FAIL — ${e.message}")
                        testsFailed++
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ❌ FAIL — Could not create turno: ${e.message}")
                testsFailed++
            }
    }

    private fun cleanupAndReport(uid: String) {
        Thread.sleep(3000)

        Log.i(TAG, "\n═══════════════════════════════════════════════════")
        Log.i(TAG, "RESULTS")
        Log.i(TAG, "═══════════════════════════════════════════════════")
        Log.i(TAG, "\n✅ PASSED: $testsPassed")
        Log.i(TAG, "❌ FAILED: $testsFailed")
        Log.i(TAG, "📊 TOTAL:  ${testsPassed + testsFailed}\n")

        if (testsFailed == 0) {
            Log.i(TAG, "🎉 ALL TESTS PASSED!\n")
        } else {
            Log.w(TAG, "⚠️  ${testsFailed} test(s) failed. Check rules.\n")
        }

        Log.i(TAG, "═══════════════════════════════════════════════════")

        auth.currentUser?.delete()?.addOnCompleteListener {
            Log.i(TAG, "Test user cleaned up")
            stopSelf()
        }
    }
}
