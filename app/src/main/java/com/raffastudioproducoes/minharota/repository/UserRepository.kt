package com.raffastudioproducoes.minharota.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.raffastudioproducoes.minharota.domain.model.User
import com.raffastudioproducoes.minharota.domain.profile.ProfileFieldPolicy

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        if (auth.currentUser?.uid != user.uid) {
            onResult(false)
            return
        }
        db.collection("usuarios").document(user.uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun updateUserField(uid: String, data: Map<String, Any>, onResult: (Boolean) -> Unit) {
        if (auth.currentUser?.uid != uid) {
            onResult(false)
            return
        }
        val safeProfileData = ProfileFieldPolicy.sanitize(data)
        if (safeProfileData.isEmpty()) {
            onResult(false)
            return
        }

        db.collection("usuarios").document(uid)
            .set(safeProfileData, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUser(uid: String, onUserLoaded: (User?) -> Unit) {
        if (auth.currentUser?.uid != uid) {
            onUserLoaded(null)
            return
        }
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { document ->
                onUserLoaded(if (document.exists()) document.toObject(User::class.java) else null)
            }
            .addOnFailureListener { onUserLoaded(null) }
    }
}
