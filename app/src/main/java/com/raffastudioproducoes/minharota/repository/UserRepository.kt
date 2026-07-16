package com.raffastudioproducoes.minharota.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.raffastudioproducoes.minharota.domain.model.User


class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        db.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUser(uid: String, onResult: (User?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(User::class.java))
            }
    }
}