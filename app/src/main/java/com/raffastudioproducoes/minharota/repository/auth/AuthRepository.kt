package com.raffastudioproducoes.minharota.repository.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

enum class AuthError {
    REQUIRED_FIELDS,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    PASSWORDS_DO_NOT_MATCH,
    INVALID_CREDENTIALS,
    TOO_MANY_REQUESTS,
    NETWORK,
    PROFILE_UPDATE_FAILED,
    EMAIL_VERIFICATION_FAILED,
    SESSION_CHANGED,
    REQUEST_FAILED,
    UNKNOWN
}

sealed interface AuthResult {
    data object Success : AuthResult
    data object ProfileIncomplete : AuthResult
    data object EmailVerificationRequired : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

enum class SessionDestination { LOGIN, VERIFY_EMAIL, COMPLETE_PROFILE, MAIN }

fun restoreSessionDestination(
    uid: String?,
    displayName: String?,
    isEmailVerified: Boolean = true,
    @Suppress("UNUSED_PARAMETER")
    providerIds: Set<String> = emptySet()
): SessionDestination = when {
    uid == null -> SessionDestination.LOGIN
    !isEmailVerified -> SessionDestination.VERIFY_EMAIL
    displayName?.trim().isNullOrEmpty() -> SessionDestination.COMPLETE_PROFILE
    else -> SessionDestination.MAIN
}

sealed interface RegistrationResult {
    data object AccountCreated : RegistrationResult
    data object ProfileUpdated : RegistrationResult
    data object VerificationSent : RegistrationResult
    data class Failure(val error: AuthError) : RegistrationResult
}

interface AuthRepository {
    val currentUserUid: String?
    val currentUserSession: AuthUserSession?
    fun login(email: String, password: String, onResult: (AuthResult) -> Unit)
    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (RegistrationResult) -> Unit
    )
    fun retryProfileUpdate(uid: String, name: String, onResult: (AuthResult) -> Unit)
    fun resendEmailVerification(uid: String, onResult: (AuthResult) -> Unit)
    fun reloadSession(uid: String, onResult: (AuthResult) -> Unit)
    fun signOut()
}

data class AuthUserSession(
    val uid: String,
    val displayName: String?,
    val isEmailVerified: Boolean,
    val providerIds: Set<String>
) {
    fun destination(): SessionDestination = restoreSessionDestination(
        uid = uid,
        displayName = displayName,
        isEmailVerified = isEmailVerified,
        providerIds = providerIds
    )
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    override val currentUserUid: String?
        get() = firebaseAuth.currentUser?.uid
    override val currentUserSession: AuthUserSession?
        get() = firebaseAuth.currentUser?.toSession()

    override fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val signedInUser = task.result?.user
                val currentUser = firebaseAuth.currentUser
                if (task.isSuccessful && signedInUser != null && currentUser?.uid == signedInUser.uid) {
                    onResult(signedInUser.toSession().toAuthResult())
                } else {
                    onResult(AuthResult.Failure(mapException(task.exception)))
                }
            }
    }

    override fun register(
        name: String,
        email: String,
        password: String,
        onResult: (RegistrationResult) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(RegistrationResult.Failure(mapException(task.exception)))
                    return@addOnCompleteListener
                }

                val createdUser = task.result?.user
                if (createdUser == null || firebaseAuth.currentUser?.uid != createdUser.uid) {
                    onResult(RegistrationResult.Failure(AuthError.UNKNOWN))
                    return@addOnCompleteListener
                }

                onResult(RegistrationResult.AccountCreated)
                updateDisplayName(createdUser, name) { result ->
                    when (result) {
                        AuthResult.Success -> {
                            onResult(RegistrationResult.ProfileUpdated)
                            sendVerification(createdUser) { verificationResult ->
                                onResult(
                                    if (verificationResult is AuthResult.EmailVerificationRequired) {
                                        RegistrationResult.VerificationSent
                                    } else {
                                        RegistrationResult.Failure(AuthError.EMAIL_VERIFICATION_FAILED)
                                    }
                                )
                            }
                        }
                        AuthResult.ProfileIncomplete -> onResult(
                            RegistrationResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
                        )
                        AuthResult.EmailVerificationRequired -> onResult(
                            RegistrationResult.Failure(AuthError.EMAIL_VERIFICATION_FAILED)
                        )
                        is AuthResult.Failure -> onResult(RegistrationResult.Failure(result.error))
                    }
                }
            }
    }

    override fun retryProfileUpdate(uid: String, name: String, onResult: (AuthResult) -> Unit) {
        val normalizedName = name.trim()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.uid != uid || normalizedName.isEmpty()) {
            onResult(AuthResult.Failure(AuthError.PROFILE_UPDATE_FAILED))
            return
        }
        updateDisplayName(currentUser, normalizedName) { result ->
            if (result !is AuthResult.Success) {
                onResult(result)
                return@updateDisplayName
            }
            val session = currentUserSession
            if (session == null || session.uid != uid) {
                onResult(AuthResult.Failure(AuthError.SESSION_CHANGED))
            } else if (session.destination() == SessionDestination.VERIFY_EMAIL) {
                sendVerification(currentUser, onResult)
            } else {
                onResult(session.toAuthResult())
            }
        }
    }

    override fun resendEmailVerification(uid: String, onResult: (AuthResult) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.uid != uid) {
            onResult(AuthResult.Failure(AuthError.SESSION_CHANGED))
            return
        }
        sendVerification(currentUser, onResult)
    }

    override fun reloadSession(uid: String, onResult: (AuthResult) -> Unit) {
        val expectedUser = firebaseAuth.currentUser
        if (expectedUser == null || expectedUser.uid != uid) {
            onResult(AuthResult.Failure(AuthError.SESSION_CHANGED))
            return
        }
        expectedUser.reload().addOnCompleteListener { task ->
            val currentSession = currentUserSession
            if (!task.isSuccessful || currentSession == null || currentSession.uid != uid) {
                onResult(AuthResult.Failure(AuthError.SESSION_CHANGED))
            } else {
                onResult(currentSession.toAuthResult())
            }
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private fun sendVerification(user: FirebaseUser, onResult: (AuthResult) -> Unit) {
        val expectedUid = user.uid
        if (firebaseAuth.currentUser?.uid != expectedUid) {
            onResult(AuthResult.Failure(AuthError.SESSION_CHANGED))
            return
        }
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful && firebaseAuth.currentUser?.uid == expectedUid) {
                onResult(AuthResult.EmailVerificationRequired)
            } else {
                onResult(AuthResult.Failure(AuthError.EMAIL_VERIFICATION_FAILED))
            }
        }
    }

    private fun updateDisplayName(
        user: FirebaseUser,
        name: String,
        onResult: (AuthResult) -> Unit
    ) {
        val expectedUid = user.uid
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            val currentUserIsStillValid = firebaseAuth.currentUser?.uid == expectedUid
            if (task.isSuccessful && currentUserIsStillValid) {
                onResult(AuthResult.Success)
            } else {
                onResult(AuthResult.Failure(AuthError.PROFILE_UPDATE_FAILED))
            }
        }
    }

    private fun mapException(exception: Exception?): AuthError = when (exception) {
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthUserCollisionException -> AuthError.REQUEST_FAILED
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
        is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
        is FirebaseNetworkException -> AuthError.NETWORK
        else -> AuthError.UNKNOWN
    }

    private fun FirebaseUser.toSession(): AuthUserSession = AuthUserSession(
        uid = uid,
        displayName = displayName,
        isEmailVerified = isEmailVerified,
        providerIds = providerData.mapTo(mutableSetOf()) { it.providerId }
    )

    private fun AuthUserSession.toAuthResult(): AuthResult = when (destination()) {
        SessionDestination.MAIN -> AuthResult.Success
        SessionDestination.COMPLETE_PROFILE -> AuthResult.ProfileIncomplete
        SessionDestination.VERIFY_EMAIL -> AuthResult.EmailVerificationRequired
        SessionDestination.LOGIN -> AuthResult.Failure(AuthError.SESSION_CHANGED)
    }
}
