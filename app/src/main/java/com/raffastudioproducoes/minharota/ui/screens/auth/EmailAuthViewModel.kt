package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.repository.auth.AuthError
import com.raffastudioproducoes.minharota.repository.auth.AuthRepository
import com.raffastudioproducoes.minharota.repository.auth.AuthResult
import com.raffastudioproducoes.minharota.repository.auth.FirebaseAuthRepository
import com.raffastudioproducoes.minharota.repository.auth.RegistrationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object ProfileCompletionRequired : AuthState
    data class EmailVerificationRequired(val resendAvailableAtMillis: Long) : AuthState
    data class Error(val error: AuthError) : AuthState
}

fun AuthState.allowsNavigation(): Boolean = this is AuthState.Authenticated

class EmailAuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isSubmitEnabled: Boolean
        get() = _authState.value !is AuthState.Loading

    data class PendingProfileUpdate(val uid: String, val normalizedName: String)

    val isProfileRecoveryPending: Boolean
        get() = pendingProfileUpdate != null

    private var pendingProfileUpdate: PendingProfileUpdate? = null
    private var operationGeneration = 0L
    private var verificationUid: String? = null
    private var resendAvailableAtMillis = 0L

    fun login(email: String, password: String) {
        if (!isSubmitEnabled) return

        validateCredentials(email, password)?.let {
            _authState.value = AuthState.Error(it)
            return
        }

        _authState.value = AuthState.Loading
        val generation = ++operationGeneration
        authRepository.login(email.trim(), password) { result ->
            if (generation == operationGeneration) handleResult(result)
        }
    }

    fun register(name: String, email: String, password: String, confirmation: String) {
        if (!isSubmitEnabled) return

        pendingProfileUpdate?.let {
            completeProfile(it.normalizedName)
            return
        }

        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            _authState.value = AuthState.Error(AuthError.REQUIRED_FIELDS)
            return
        }

        validateCredentials(email, password)?.let {
            _authState.value = AuthState.Error(it)
            return
        }
        if (confirmation.isBlank()) {
            _authState.value = AuthState.Error(AuthError.REQUIRED_FIELDS)
            return
        }
        if (password != confirmation) {
            _authState.value = AuthState.Error(AuthError.PASSWORDS_DO_NOT_MATCH)
            return
        }

        _authState.value = AuthState.Loading
        val generation = ++operationGeneration
        authRepository.register(normalizedName, email.trim(), password) { result ->
            if (generation != operationGeneration) return@register
            when (result) {
                RegistrationResult.AccountCreated -> {
                    val uid = authRepository.currentUserUid
                    if (uid == null) {
                        _authState.value = AuthState.Error(AuthError.PROFILE_UPDATE_FAILED)
                    } else {
                        pendingProfileUpdate = PendingProfileUpdate(uid, normalizedName)
                    }
                }
                RegistrationResult.ProfileUpdated -> Unit
                RegistrationResult.VerificationSent -> {
                    val pending = pendingProfileUpdate
                    if (pending != null && authRepository.currentUserUid == pending.uid) {
                        pendingProfileUpdate = null
                        enterEmailVerification(pending.uid)
                    } else {
                        pendingProfileUpdate = null
                        _authState.value = AuthState.Error(AuthError.PROFILE_UPDATE_FAILED)
                    }
                }
                is RegistrationResult.Failure -> {
                    _authState.value = AuthState.Error(result.error)
                }
            }
        }
    }

    fun startProfileCompletion() {
        val uid = authRepository.currentUserUid
        pendingProfileUpdate = if (uid == null) null else PendingProfileUpdate(uid, "")
        _authState.value = if (uid == null) {
            AuthState.Error(AuthError.PROFILE_UPDATE_FAILED)
        } else {
            AuthState.ProfileCompletionRequired
        }
    }

    fun completeProfile(name: String) {
        if (!isSubmitEnabled) return
        val normalizedName = name.trim()
        val pending = pendingProfileUpdate
        val currentUid = authRepository.currentUserUid
        if (pending == null || normalizedName.isEmpty() || currentUid == null || currentUid != pending.uid) {
            pendingProfileUpdate = null
            _authState.value = AuthState.Error(
                if (normalizedName.isEmpty()) AuthError.REQUIRED_FIELDS else AuthError.PROFILE_UPDATE_FAILED
            )
            return
        }

        val recovery = PendingProfileUpdate(pending.uid, normalizedName)
        pendingProfileUpdate = recovery
        val generation = ++operationGeneration
        _authState.value = AuthState.Loading
        authRepository.retryProfileUpdate(recovery.uid, recovery.normalizedName) { result ->
            if (generation != operationGeneration) return@retryProfileUpdate
            if (authRepository.currentUserUid != recovery.uid) {
                pendingProfileUpdate = null
                _authState.value = AuthState.Error(AuthError.PROFILE_UPDATE_FAILED)
                return@retryProfileUpdate
            }
            handleProfileRecovery(result)
        }
    }

    fun logout() {
        operationGeneration++
        pendingProfileUpdate = null
        verificationUid = null
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }

    fun startEmailVerification() {
        val session = authRepository.currentUserSession
        if (session == null) {
            verificationUid = null
            _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
            return
        }
        when (session.destination()) {
            com.raffastudioproducoes.minharota.repository.auth.SessionDestination.VERIFY_EMAIL -> {
                enterEmailVerification(session.uid)
            }
            com.raffastudioproducoes.minharota.repository.auth.SessionDestination.COMPLETE_PROFILE -> {
                _authState.value = AuthState.ProfileCompletionRequired
            }
            com.raffastudioproducoes.minharota.repository.auth.SessionDestination.MAIN -> {
                _authState.value = AuthState.Authenticated
            }
            com.raffastudioproducoes.minharota.repository.auth.SessionDestination.LOGIN -> {
                _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
            }
        }
    }

    fun resendVerificationEmail() {
        if (!isSubmitEnabled || currentTimeMillis() < resendAvailableAtMillis) return
        val expectedUid = verificationUid
        if (expectedUid == null || authRepository.currentUserUid != expectedUid) {
            verificationUid = null
            _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
            return
        }
        val generation = ++operationGeneration
        _authState.value = AuthState.Loading
        authRepository.resendEmailVerification(expectedUid) { result ->
            if (generation != operationGeneration) return@resendEmailVerification
            if (authRepository.currentUserUid != expectedUid) {
                verificationUid = null
                _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
                return@resendEmailVerification
            }
            when (result) {
                AuthResult.EmailVerificationRequired -> enterEmailVerification(expectedUid)
                is AuthResult.Failure -> _authState.value = AuthState.Error(result.error)
                else -> _authState.value = AuthState.Error(AuthError.EMAIL_VERIFICATION_FAILED)
            }
        }
    }

    fun checkEmailVerification() {
        if (!isSubmitEnabled) return
        val expectedUid = verificationUid
        if (expectedUid == null || authRepository.currentUserUid != expectedUid) {
            verificationUid = null
            _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
            return
        }
        val generation = ++operationGeneration
        _authState.value = AuthState.Loading
        authRepository.reloadSession(expectedUid) { result ->
            if (generation != operationGeneration) return@reloadSession
            if (authRepository.currentUserUid != expectedUid) {
                verificationUid = null
                _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
                return@reloadSession
            }
            if (result is AuthResult.EmailVerificationRequired) {
                verificationUid = expectedUid
                _authState.value = AuthState.EmailVerificationRequired(resendAvailableAtMillis)
            } else {
                handleResult(result)
            }
        }
    }

    fun cooldownRemainingSeconds(): Long =
        ((resendAvailableAtMillis - currentTimeMillis()).coerceAtLeast(0L) + 999L) / 1000L

    val canResendVerification: Boolean
        get() = isSubmitEnabled && verificationUid != null && cooldownRemainingSeconds() == 0L

    private fun validateCredentials(email: String, password: String): AuthError? = when {
        email.isBlank() || password.isBlank() -> AuthError.REQUIRED_FIELDS
        !EMAIL_REGEX.matches(email.trim()) -> AuthError.INVALID_EMAIL
        password.length < MIN_PASSWORD_LENGTH -> AuthError.WEAK_PASSWORD
        else -> null
    }

    private fun handleResult(result: AuthResult) {
        when (result) {
            AuthResult.Success -> _authState.value = AuthState.Authenticated
            AuthResult.ProfileIncomplete -> startProfileCompletion()
            AuthResult.EmailVerificationRequired -> {
                val uid = authRepository.currentUserUid
                if (uid == null) {
                    _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
                } else {
                    enterEmailVerification(uid)
                }
            }
            is AuthResult.Failure -> _authState.value = AuthState.Error(result.error)
        }
    }

    private fun handleProfileRecovery(result: AuthResult) {
        when (result) {
            AuthResult.Success -> {
                pendingProfileUpdate = null
                _authState.value = AuthState.Authenticated
            }
            AuthResult.ProfileIncomplete -> {
                _authState.value = AuthState.ProfileCompletionRequired
            }
            AuthResult.EmailVerificationRequired -> {
                pendingProfileUpdate = null
                val uid = authRepository.currentUserUid
                if (uid == null) {
                    _authState.value = AuthState.Error(AuthError.SESSION_CHANGED)
                } else {
                    enterEmailVerification(uid)
                }
            }
            is AuthResult.Failure -> {
                _authState.value = AuthState.Error(result.error)
            }
        }
    }

    private fun enterEmailVerification(uid: String) {
        verificationUid = uid
        resendAvailableAtMillis = currentTimeMillis() + RESEND_COOLDOWN_MILLIS
        _authState.value = AuthState.EmailVerificationRequired(resendAvailableAtMillis)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        const val RESEND_COOLDOWN_MILLIS = 60_000L
        val EMAIL_REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    }
}
