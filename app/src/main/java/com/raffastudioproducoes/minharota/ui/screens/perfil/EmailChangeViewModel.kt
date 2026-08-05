package com.raffastudioproducoes.minharota.ui.screens.perfil

import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.repository.auth.EmailChangeRepository
import com.raffastudioproducoes.minharota.repository.auth.EmailChangeResult
import com.raffastudioproducoes.minharota.repository.auth.EmailReloadResult
import com.raffastudioproducoes.minharota.repository.auth.ConfirmedEmailReconciler
import com.raffastudioproducoes.minharota.repository.auth.FirebaseConfirmedEmailReconciler
import com.raffastudioproducoes.minharota.repository.auth.FirebaseEmailChangeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface EmailChangeState {
    data object Idle : EmailChangeState
    data object Loading : EmailChangeState
    data object CheckingConfirmation : EmailChangeState
    data object VerificationEmailSent : EmailChangeState
    data class CurrentEmailLoaded(val confirmedEmail: String) : EmailChangeState
    data class ReconciliationError(val confirmedEmail: String) : EmailChangeState
    data object ReauthenticationRequired : EmailChangeState
    data object NetworkError : EmailChangeState
    data object InvalidEmail : EmailChangeState
    data object Error : EmailChangeState
}

class EmailChangeViewModel(
    private val repository: EmailChangeRepository = FirebaseEmailChangeRepository(),
    reconciler: ConfirmedEmailReconciler? = null
) : ViewModel() {
    private val confirmedEmailReconciler by lazy {
        reconciler ?: FirebaseConfirmedEmailReconciler()
    }
    private val _state = MutableStateFlow<EmailChangeState>(EmailChangeState.Idle)
    val state: StateFlow<EmailChangeState> = _state.asStateFlow()

    private var operationGeneration = 0L
    private var pendingEmail: String? = null
    val hasPendingRequest: Boolean
        get() = pendingEmail != null

    fun requestChange(email: String) {
        if (operationInProgress()) return
        val normalizedEmail = email.trim()
        if (!EMAIL_REGEX.matches(normalizedEmail)) {
            pendingEmail = null
            _state.value = EmailChangeState.InvalidEmail
            return
        }
        val expectedUid = repository.currentUid
        if (expectedUid == null) {
            pendingEmail = null
            _state.value = EmailChangeState.Error
            return
        }

        val generation = ++operationGeneration
        pendingEmail = normalizedEmail
        _state.value = EmailChangeState.Loading
        repository.requestVerification(expectedUid, normalizedEmail) { result ->
            if (generation != operationGeneration) return@requestVerification
            pendingEmail = null
            if (repository.currentUid != expectedUid) {
                _state.value = EmailChangeState.Error
                return@requestVerification
            }
            _state.value = when (result) {
                EmailChangeResult.VerificationSent -> EmailChangeState.VerificationEmailSent
                EmailChangeResult.ReauthenticationRequired -> {
                    EmailChangeState.ReauthenticationRequired
                }
                EmailChangeResult.NetworkFailure -> EmailChangeState.NetworkError
                EmailChangeResult.GenericFailure -> EmailChangeState.Error
            }
        }
    }

    fun checkForConfirmedEmail() {
        if (operationInProgress()) return
        val expectedUid = repository.currentUid
        if (expectedUid == null) {
            _state.value = EmailChangeState.Error
            return
        }
        val generation = ++operationGeneration
        var reloadHandled = false
        _state.value = EmailChangeState.CheckingConfirmation
        repository.reloadConfirmedEmail(expectedUid) { result ->
            if (reloadHandled || generation != operationGeneration) return@reloadConfirmedEmail
            reloadHandled = true
            if (repository.currentUid != expectedUid || result !is EmailReloadResult.Success) {
                _state.value = EmailChangeState.Error
                return@reloadConfirmedEmail
            }
            var reconciliationHandled = false
            confirmedEmailReconciler.reconcile(expectedUid, result.confirmedEmail) { succeeded ->
                if (reconciliationHandled || generation != operationGeneration) return@reconcile
                reconciliationHandled = true
                if (repository.currentUid != expectedUid) {
                    _state.value = EmailChangeState.Error
                    return@reconcile
                }
                _state.value = if (succeeded) {
                    EmailChangeState.CurrentEmailLoaded(result.confirmedEmail)
                } else {
                    EmailChangeState.ReconciliationError(result.confirmedEmail)
                }
            }
        }
    }

    fun cancelOperation() {
        operationGeneration++
        pendingEmail = null
        _state.value = EmailChangeState.Idle
    }

    fun onLogout() = cancelOperation()

    private fun operationInProgress(): Boolean =
        _state.value is EmailChangeState.Loading ||
            _state.value is EmailChangeState.CheckingConfirmation

    private companion object {
        val EMAIL_REGEX = Regex(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            RegexOption.IGNORE_CASE
        )
    }
}
