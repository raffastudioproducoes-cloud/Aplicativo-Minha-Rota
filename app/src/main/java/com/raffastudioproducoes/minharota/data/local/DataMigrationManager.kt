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

        val db = FirebaseFirestore.getInstance()

        // 1. Vincula APENAS os dados da página de perfil/cadastro inicial
        val dadosPerfil = mutableMapOf<String, Any>(
            "email" to (currentUser.email ?: ""),
            "name" to (currentUser.displayName ?: "Motorista")
        )

        db.collection("usuarios").document(uid)
            .set(dadosPerfil, SetOptions.merge())
            .addOnSuccessListener {
                // Dados operacionais permanecem locais até que um backend
                // valide uma assinatura Pro e coordene o backup.
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

}
