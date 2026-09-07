package com.raffastudioproducoes.minharota.testing

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raffastudioproducoes.minharota.R
import java.util.UUID

/**
 * Firestore Rules Integration Tests
 *
 * Executa testes reais contra Firestore (emulador ou production)
 * e registra resultados em Logcat.
 *
 * Usar:
 * 1. Emulator rodando: firebase emulators:start --only firestore,auth
 * 2. App conectado ao emulador (verificar MinhaRotaApp.kt)
 * 3. Abrir Activity no Android Studio
 * 4. Ler resultados em Logcat (tag: "FIRESTORE_TEST")
 */
class FirestoreRulesTestActivity : AppCompatActivity() {

    private val TAG = "FIRESTORE_TEST"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var testsPassed = 0
    private var testsFailed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sem UI — testes rodam em background nos logs

        Log.i(TAG, "═══════════════════════════════════════════════════")
        Log.i(TAG, "FIRESTORE RULES TESTS — Starting")
        Log.i(TAG, "═══════════════════════════════════════════════════")

        // Criar usuário de teste e rodar testes
        createTestUserAndRunTests()
    }

    private fun createTestUserAndRunTests() {
        val testEmail = "test-${UUID.randomUUID().toString().take(8)}@test.local"
        val testPassword = "Test@12345"

        // Criar usuário de teste
        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                Log.i(TAG, "\n✅ Test user created: $uid")
                Log.i(TAG, "   Email: $testEmail")

                // Rodar todos os testes com este usuário
                runAllTests(uid)

                // Limpar e reportar resultados
                cleanupAndReport(uid)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to create test user: ${e.message}", e)
            }
    }

    private fun runAllTests(uid: String) {
        Log.i(TAG, "\n------- TEST SUITE -------\n")

        // TEST 1: CREATE Turno válido
        test1_CreateValidTurno(uid)

        // TEST 2: CREATE Turno sem ID
        test2_CreateTurnoWithoutId(uid)

        // TEST 3: UPDATE Usuario (proteger isPro)
        test3_UpdateUsuarioBlockPro(uid)

        // TEST 4: READ do outro usuário
        test4_ReadAnotherUsersData(uid)

        // TEST 5: CREATE sem autenticação
        test5_CreateWithoutAuth()

        // TEST 6: DELETE próprio turno
        test6_DeleteOwnTurno(uid)
    }

    // ═══════════════════════════════════════════════════════════════════════════

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
            "corridas" to emptyList<Any>()
        )

        db.collection("usuarios")
            .document(uid)
            .collection("turnos")
            .document(turnoId)
            .set(turnoData)
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
            // "id" FALTA!
            "data" to "07/09/2024",
            "horaInicio" to "08:00",
            "ganhoBruto" to 150.50
        )

        db.collection("usuarios")
            .document(uid)
            .collection("turnos")
            .document(turnoId)
            .set(turnoData)
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

        // Primeiro criar documento usuario
        val userData = mapOf(
            "nome" to "Test User",
            "email" to "test@example.com"
        )

        db.collection("usuarios")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                // Depois tentar atualizar campo protegido
                db.collection("usuarios")
                    .document(uid)
                    .update(mapOf(
                        "isPro" to true,  // Protegido
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

        db.collection("usuarios")
            .document(otherUserId)
            .collection("turnos")
            .document("some-turno")
            .get()
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

        // Fazer logout
        auth.signOut()

        val userData = mapOf(
            "nome" to "Unauthorized",
            "email" to "unauth@example.com"
        )

        db.collection("usuarios")
            .document(currentUid)
            .set(userData)
            .addOnSuccessListener {
                Log.e(TAG, "  ❌ FAIL — Should have been DENIED (no auth)")
                testsFailed++
                // Fazer login novamente para próximos testes
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
                // Fazer login novamente
                auth.signInWithEmailAndPassword("test@example.com", "Test@12345").addOnCompleteListener { }
            }
    }

    private fun test6_DeleteOwnTurno(uid: String) {
        Log.i(TAG, "\n[TEST 6] DELETE own Turno (should ALLOW)")

        val turnoId = "turno-to-delete-${UUID.randomUUID().toString().take(8)}"

        // Criar turno primeiro
        val turnoData = mapOf(
            "id" to turnoId,
            "data" to "07/09/2024",
            "ganhoBruto" to 100.0
        )

        db.collection("usuarios")
            .document(uid)
            .collection("turnos")
            .document(turnoId)
            .set(turnoData)
            .addOnSuccessListener {
                // Depois deletar
                db.collection("usuarios")
                    .document(uid)
                    .collection("turnos")
                    .document(turnoId)
                    .delete()
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

    // ═══════════════════════════════════════════════════════════════════════════

    private fun cleanupAndReport(uid: String) {
        // Aguardar um pouco para todas as operações completarem
        Thread.sleep(2000)

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
        Log.i(TAG, "Copy logs to: firebase/FIRESTORE_RULES_TEST_RESULTS.txt")
        Log.i(TAG, "═══════════════════════════════════════════════════")

        // Deletar usuário de teste
        auth.currentUser?.delete()
            ?.addOnCompleteListener {
                Log.i(TAG, "Test user cleaned up")
            }
    }
}
