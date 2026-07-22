package com.raffastudioproducoes.minharota.data.local

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


object DataMigrationManager {

    fun migrarDadosDoConvidadoParaNuvem(context: Context, onComplete: () -> Unit = {}) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: run {
            onComplete()
            return
        }

        val prefs = SharedPreferencesManager(context)
        val db = FirebaseFirestore.getInstance()

        val isPro = prefs.obterIsPro()
        val nomePlano = prefs.obterNomePlano()
        val dataVencimento = prefs.obterDataVencimento()

        // 1. Vincula APENAS os dados da página de perfil/cadastro inicial
        val dadosPerfil = mutableMapOf<String, Any>(
            "email" to (currentUser.email ?: ""),
            "nome" to (currentUser.displayName ?: "Motorista"),
            "isPro" to isPro,
            "nomePlano" to nomePlano
        )
        if (dataVencimento.isNotBlank()) {
            dadosPerfil["dataVencimento"] = dataVencimento
        }

        db.collection("usuarios").document(uid)
            .set(dadosPerfil, SetOptions.merge())
            .addOnSuccessListener {
                // 2. O restante dos dados (contas, caixinhas, turnos) SÓ é migrado
                // se o usuário realmente possuir plano Pro ou Premium ativo.
                if (isPro) {
                    sincronizarDadosPro(uid, prefs, db)
                }
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    private fun sincronizarDadosPro(
        uid: String,
        prefs: SharedPreferencesManager,
        db: FirebaseFirestore
    ) {
        // Migrar Contas Fixas (Apenas para Pro/Premium)
        val contas = prefs.obterContas()
        if (contas.isNotEmpty()) {
            val contasRef = db.collection("usuarios").document(uid).collection("contas")
            contas.forEach { conta ->
                contasRef.document(conta.id).set(conta, SetOptions.merge())
            }
        }

        // Migrar Caixinhas (Apenas para Pro/Premium)
        val caixinhas = prefs.obterCaixinhas()
        if (caixinhas.isNotEmpty()) {
            val caixasRef = db.collection("usuarios").document(uid).collection("caixinhas")
            caixinhas.forEach { caixinha ->
                caixasRef.document(caixinha.id).set(caixinha, SetOptions.merge())
            }
        }

        // Migrar Turnos (Apenas para Pro/Premium)
        val turnos = prefs.obterTurnos()
        if (turnos.isNotEmpty()) {
            val turnosRef = db.collection("usuarios").document(uid).collection("turnos")
            turnos.forEach { turno ->
                turnosRef.add(turno)
            }
        }
    }
}