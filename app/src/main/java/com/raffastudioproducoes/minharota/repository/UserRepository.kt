package com.raffastudioproducoes.minharota.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.raffastudioproducoes.minharota.domain.model.User


class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        db.collection("users").document(user.uid)
            .set(user, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUser(uid: String, onUserLoaded: (User?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                onUserLoaded(document.toObject(User::class.java))
            }
            .addOnFailureListener { onUserLoaded(null) }
    }
}