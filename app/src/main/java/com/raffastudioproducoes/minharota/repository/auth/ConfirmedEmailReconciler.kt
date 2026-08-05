package com.raffastudioproducoes.minharota.repository.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager

interface ConfirmedEmailReconciler {
    fun reconcile(
        expectedUid: String,
        confirmedEmail: String,
        onComplete: (Boolean) -> Unit
    )
}

class FirebaseConfirmedEmailReconciler(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val applicationContext: Context = FirebaseAuth.getInstance().app.applicationContext
) : ConfirmedEmailReconciler {
    override fun reconcile(
        expectedUid: String,
        confirmedEmail: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (auth.currentUser?.uid != expectedUid) {
            onComplete(false)
            return
        }
        firestore.collection("usuarios").document(expectedUid)
            .update("email", confirmedEmail)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful || auth.currentUser?.uid != expectedUid) {
                    onComplete(false)
                    return@addOnCompleteListener
                }
                SharedPreferencesManager(applicationContext).salvarEmail(confirmedEmail)
                onComplete(true)
            }
    }
}
