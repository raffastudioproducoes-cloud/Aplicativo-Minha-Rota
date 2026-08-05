package com.raffastudioproducoes.minharota.repository.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

sealed interface EmailChangeResult {
    data object VerificationSent : EmailChangeResult
    data object ReauthenticationRequired : EmailChangeResult
    data object NetworkFailure : EmailChangeResult
    data object GenericFailure : EmailChangeResult
}

sealed interface EmailReloadResult {
    data class Success(val confirmedEmail: String) : EmailReloadResult
    data object GenericFailure : EmailReloadResult
}

interface EmailChangeGateway {
    val currentUid: String?
    fun verifyBeforeUpdateEmail(
        normalizedEmail: String,
        onResult: (EmailChangeResult) -> Unit
    )
    fun reloadCurrentUser(onResult: (EmailReloadResult) -> Unit)
}

interface EmailChangeRepository {
    val currentUid: String?
    fun requestVerification(
        expectedUid: String,
        normalizedEmail: String,
        onResult: (EmailChangeResult) -> Unit
    )
    fun reloadConfirmedEmail(
        expectedUid: String,
        onResult: (EmailReloadResult) -> Unit
    )
}

class FirebaseEmailChangeRepository(
    private val gateway: EmailChangeGateway = FirebaseEmailChangeGateway()
) : EmailChangeRepository {
    override val currentUid: String?
        get() = gateway.currentUid

    override fun requestVerification(
        expectedUid: String,
        normalizedEmail: String,
        onResult: (EmailChangeResult) -> Unit
    ) {
        if (gateway.currentUid != expectedUid) {
            onResult(EmailChangeResult.GenericFailure)
            return
        }
        gateway.verifyBeforeUpdateEmail(normalizedEmail) { result ->
            if (gateway.currentUid == expectedUid) {
                onResult(result)
            } else {
                onResult(EmailChangeResult.GenericFailure)
            }
        }
    }

    override fun reloadConfirmedEmail(
        expectedUid: String,
        onResult: (EmailReloadResult) -> Unit
    ) {
        if (gateway.currentUid != expectedUid) {
            onResult(EmailReloadResult.GenericFailure)
            return
        }
        gateway.reloadCurrentUser { result ->
            if (gateway.currentUid == expectedUid) {
                onResult(result)
            } else {
                onResult(EmailReloadResult.GenericFailure)
            }
        }
    }
}

private class FirebaseEmailChangeGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : EmailChangeGateway {
    override val currentUid: String?
        get() = auth.currentUser?.uid

    override fun verifyBeforeUpdateEmail(
        normalizedEmail: String,
        onResult: (EmailChangeResult) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(EmailChangeResult.GenericFailure)
            return
        }
        user.verifyBeforeUpdateEmail(normalizedEmail).addOnCompleteListener { task ->
            val result = when {
                task.isSuccessful -> EmailChangeResult.VerificationSent
                task.exception is FirebaseAuthRecentLoginRequiredException -> {
                    EmailChangeResult.ReauthenticationRequired
                }
                task.exception is FirebaseNetworkException -> EmailChangeResult.NetworkFailure
                else -> EmailChangeResult.GenericFailure
            }
            onResult(result)
        }
    }

    override fun reloadCurrentUser(onResult: (EmailReloadResult) -> Unit) {
        val expectedUser = auth.currentUser
        if (expectedUser == null) {
            onResult(EmailReloadResult.GenericFailure)
            return
        }
        val expectedUid = expectedUser.uid
        expectedUser.reload().addOnCompleteListener { task ->
            val currentUser = auth.currentUser
            if (!task.isSuccessful || currentUser?.uid != expectedUid) {
                onResult(EmailReloadResult.GenericFailure)
                return@addOnCompleteListener
            }
            val confirmedEmail = currentUser.email?.trim().orEmpty()
            if (confirmedEmail.isEmpty()) {
                onResult(EmailReloadResult.GenericFailure)
            } else {
                onResult(EmailReloadResult.Success(confirmedEmail))
            }
        }
    }
}
