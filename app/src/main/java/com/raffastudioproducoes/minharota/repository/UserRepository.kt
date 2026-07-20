package com.raffastudioproducoes.minharota.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.raffastudioproducoes.minharota.domain.model.User


class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        db.collection("usuarios").document(user.uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Usuário salvo com sucesso no Firestore!")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Erro ao salvar usuário", e)
                onResult(false)
            }
    }

    fun updateUserField(uid: String, data: Map<String, Any>, onResult: (Boolean) -> Unit) {
        db.collection("usuarios").document(uid)
            .set(
                data,
                SetOptions.merge()
            ) // Usar set com merge evita o erro de documento inexistente
            .addOnSuccessListener {
                Log.d("UserRepository", "Campo atualizado com sucesso!")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Erro ao atualizar campo", e)
                onResult(false)
            }
    }

    fun getUser(uid: String, onUserLoaded: (User?) -> Unit) {
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onUserLoaded(document.toObject(User::class.java))
                } else {
                    onUserLoaded(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Erro ao buscar usuário", e)
                onUserLoaded(null)
            }
    }
}

