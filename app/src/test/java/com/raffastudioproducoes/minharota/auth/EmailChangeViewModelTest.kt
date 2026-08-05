package com.raffastudioproducoes.minharota.auth

import com.raffastudioproducoes.minharota.repository.auth.EmailChangeGateway
import com.raffastudioproducoes.minharota.repository.auth.EmailChangeRepository
import com.raffastudioproducoes.minharota.repository.auth.EmailChangeResult
import com.raffastudioproducoes.minharota.repository.auth.EmailReloadResult
import com.raffastudioproducoes.minharota.repository.auth.ConfirmedEmailReconciler
import com.raffastudioproducoes.minharota.repository.auth.FirebaseEmailChangeRepository
import com.raffastudioproducoes.minharota.ui.screens.perfil.EmailChangeState
import com.raffastudioproducoes.minharota.ui.screens.perfil.EmailChangeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailChangeViewModelTest {
    @Test
    fun `usuario ausente nao inicia alteracao`() {
        val repository = FakeRepository(currentUid = null)
        val viewModel = EmailChangeViewModel(repository)

        viewModel.requestChange("novo@example.com")

        assertEquals(0, repository.requests)
        assertTrue(viewModel.state.value is EmailChangeState.Error)
    }

    @Test
    fun `uid correto permite solicitar verificacao`() {
        val repository = FakeRepository(currentUid = "user-a")
        val viewModel = EmailChangeViewModel(repository)

        viewModel.requestChange("novo@example.com")

        assertEquals(listOf("user-a" to "novo@example.com"), repository.requested)
    }

    @Test
    fun `sucesso informa envio pendente e nao alteracao concluida`() {
        val viewModel = EmailChangeViewModel(FakeRepository(result = EmailChangeResult.VerificationSent))

        viewModel.requestChange("novo@example.com")

        assertEquals(EmailChangeState.VerificationEmailSent, viewModel.state.value)
        assertFalse(viewModel.hasPendingRequest)
    }

    @Test
    fun `colisao recebe resposta publica generica`() {
        val collision = EmailChangeViewModel(FakeRepository(result = EmailChangeResult.GenericFailure))
        val internal = EmailChangeViewModel(FakeRepository(result = EmailChangeResult.GenericFailure))

        collision.requestChange("ocupado@example.com")
        internal.requestChange("novo@example.com")

        assertEquals(collision.state.value, internal.state.value)
        assertEquals(EmailChangeState.Error, collision.state.value)
    }

    @Test
    fun `reauthentication required preserva apenas estado seguro`() {
        val viewModel = EmailChangeViewModel(
            FakeRepository(result = EmailChangeResult.ReauthenticationRequired)
        )

        viewModel.requestChange("novo@example.com")

        assertEquals(EmailChangeState.ReauthenticationRequired, viewModel.state.value)
        assertFalse(viewModel.hasPendingRequest)
    }

    @Test
    fun `uid alterado antes do callback invalida resultado`() {
        val repository = FakeRepository(currentUid = "user-a", completeImmediately = false)
        val viewModel = EmailChangeViewModel(repository)
        viewModel.requestChange("novo@example.com")
        repository.currentUid = "user-b"

        repository.emit(EmailChangeResult.VerificationSent)

        assertEquals(EmailChangeState.Error, viewModel.state.value)
    }

    @Test
    fun `currentUser nulo no callback invalida resultado`() {
        val repository = FakeRepository(currentUid = "user-a", completeImmediately = false)
        val viewModel = EmailChangeViewModel(repository)
        viewModel.requestChange("novo@example.com")
        repository.currentUid = null

        repository.emit(EmailChangeResult.VerificationSent)

        assertEquals(EmailChangeState.Error, viewModel.state.value)
    }

    @Test
    fun `logout invalida callback atrasado`() {
        val repository = FakeRepository(completeImmediately = false)
        val viewModel = EmailChangeViewModel(repository)
        viewModel.requestChange("novo@example.com")

        viewModel.onLogout()
        repository.emit(EmailChangeResult.VerificationSent)

        assertEquals(EmailChangeState.Idle, viewModel.state.value)
        assertFalse(viewModel.hasPendingRequest)
    }

    @Test
    fun `submissao duplicada durante loading e ignorada`() {
        val repository = FakeRepository(completeImmediately = false)
        val viewModel = EmailChangeViewModel(repository)

        viewModel.requestChange("primeiro@example.com")
        viewModel.requestChange("segundo@example.com")

        assertEquals(1, repository.requests)
        assertEquals(EmailChangeState.Loading, viewModel.state.value)
    }

    @Test
    fun `nova operacao invalida callback anterior`() {
        val repository = FakeRepository(completeImmediately = false)
        val viewModel = EmailChangeViewModel(repository)
        viewModel.requestChange("primeiro@example.com")
        val firstCallback = repository.callbacks.single()

        viewModel.cancelOperation()
        viewModel.requestChange("segundo@example.com")
        firstCallback(EmailChangeResult.VerificationSent)

        assertEquals(EmailChangeState.Loading, viewModel.state.value)
        assertEquals(2, repository.requests)
    }

    @Test
    fun `endereco invalido e rejeitado localmente`() {
        val repository = FakeRepository()
        val viewModel = EmailChangeViewModel(repository)

        viewModel.requestChange("email-invalido")

        assertEquals(0, repository.requests)
        assertEquals(EmailChangeState.InvalidEmail, viewModel.state.value)
    }

    @Test
    fun `repositorio solicita verificacao sem concluir alteracao imediatamente`() {
        val gateway = FakeGateway(currentUid = "user-a")
        val repository = FirebaseEmailChangeRepository(gateway)
        var result: EmailChangeResult? = null

        repository.requestVerification("user-a", "novo@example.com") { result = it }

        assertEquals(listOf("novo@example.com"), gateway.verificationRequests)
        assertNull(result)
    }

    @Test
    fun `repositorio recusa uid incorreto antes da chamada`() {
        val gateway = FakeGateway(currentUid = "user-b")
        val repository = FirebaseEmailChangeRepository(gateway)
        var result: EmailChangeResult? = null

        repository.requestVerification("user-a", "novo@example.com") { result = it }

        assertTrue(gateway.verificationRequests.isEmpty())
        assertEquals(EmailChangeResult.GenericFailure, result)
    }

    @Test
    fun `repositorio invalida troca de uid no callback`() {
        val gateway = FakeGateway(currentUid = "user-a")
        val repository = FirebaseEmailChangeRepository(gateway)
        var result: EmailChangeResult? = null
        repository.requestVerification("user-a", "novo@example.com") { result = it }
        gateway.currentUid = "user-b"

        gateway.emit(EmailChangeResult.VerificationSent)

        assertEquals(EmailChangeResult.GenericFailure, result)
    }

    @Test
    fun `reload bem sucedido publica somente email confirmado pelo Firebase`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.requestChange("pendente@example.com")
        repository.emit(EmailChangeResult.VerificationSent)

        viewModel.checkForConfirmedEmail()
        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(
            EmailChangeState.CurrentEmailLoaded("confirmado@example.com"),
            viewModel.state.value
        )
        assertEquals(listOf("confirmado@example.com"), reconciler.emails)
    }

    @Test
    fun `endereco pendente nunca e usado como confirmacao`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.requestChange("pendente@example.com")
        repository.emit(EmailChangeResult.VerificationSent)

        viewModel.checkForConfirmedEmail()
        repository.emitReload(EmailReloadResult.Success("atual@example.com"))

        assertEquals(listOf("atual@example.com"), reconciler.emails)
        assertEquals(
            EmailChangeState.CurrentEmailLoaded("atual@example.com"),
            viewModel.state.value
        )
    }

    @Test
    fun `reload sem mudanca mantem email atual retornado pelo Firebase`() {
        val repository = FakeRepository(
            reloadResult = EmailReloadResult.Success("atual@example.com")
        )
        val viewModel = EmailChangeViewModel(repository, FakeReconciler())

        viewModel.checkForConfirmedEmail()

        assertEquals(
            EmailChangeState.CurrentEmailLoaded("atual@example.com"),
            viewModel.state.value
        )
    }

    @Test
    fun `currentUser nulo depois do reload invalida resultado`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.checkForConfirmedEmail()
        repository.currentUid = null

        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(EmailChangeState.Error, viewModel.state.value)
        assertTrue(reconciler.emails.isEmpty())
    }

    @Test
    fun `mudanca de uid depois do reload invalida resultado`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.checkForConfirmedEmail()
        repository.currentUid = "user-b"

        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(EmailChangeState.Error, viewModel.state.value)
        assertTrue(reconciler.emails.isEmpty())
    }

    @Test
    fun `logout invalida callback atrasado do reload`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.checkForConfirmedEmail()

        viewModel.onLogout()
        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(EmailChangeState.Idle, viewModel.state.value)
        assertTrue(reconciler.emails.isEmpty())
    }

    @Test
    fun `operacao mais nova invalida callback anterior do reload`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.checkForConfirmedEmail()
        val oldCallback = repository.reloadCallbacks.single()
        viewModel.cancelOperation()
        viewModel.checkForConfirmedEmail()

        oldCallback(EmailReloadResult.Success("antigo@example.com"))

        assertTrue(viewModel.state.value is EmailChangeState.CheckingConfirmation)
        assertTrue(reconciler.emails.isEmpty())
    }

    @Test
    fun `callback duplicado nao repete reconciliacao`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)
        viewModel.checkForConfirmedEmail()

        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))
        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(1, reconciler.emails.size)
    }

    @Test
    fun `reconciliacao secundaria ocorre somente depois do reload confirmado`() {
        val repository = FakeRepository(completeReloadImmediately = false)
        val reconciler = FakeReconciler()
        val viewModel = EmailChangeViewModel(repository, reconciler)

        viewModel.checkForConfirmedEmail()
        assertTrue(reconciler.emails.isEmpty())
        repository.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(listOf("confirmado@example.com"), reconciler.emails)
    }

    @Test
    fun `falha de reconciliacao preserva email confirmado do Firebase`() {
        val repository = FakeRepository(
            reloadResult = EmailReloadResult.Success("confirmado@example.com")
        )
        val reconciler = FakeReconciler(succeeds = false)
        val viewModel = EmailChangeViewModel(repository, reconciler)

        viewModel.checkForConfirmedEmail()

        assertEquals(
            EmailChangeState.ReconciliationError("confirmado@example.com"),
            viewModel.state.value
        )
    }

    @Test
    fun `repositorio recarrega usuario esperado e retorna email do gateway`() {
        val gateway = FakeGateway(currentUid = "user-a")
        val repository = FirebaseEmailChangeRepository(gateway)
        var result: EmailReloadResult? = null

        repository.reloadConfirmedEmail("user-a") { result = it }
        gateway.emitReload(EmailReloadResult.Success("confirmado@example.com"))

        assertEquals(EmailReloadResult.Success("confirmado@example.com"), result)
    }

    private class FakeRepository(
        override var currentUid: String? = "user-a",
        private val result: EmailChangeResult = EmailChangeResult.VerificationSent,
        private val completeImmediately: Boolean = true,
        private val reloadResult: EmailReloadResult = EmailReloadResult.Success("atual@example.com"),
        private val completeReloadImmediately: Boolean = true
    ) : EmailChangeRepository {
        var requests = 0
        val requested = mutableListOf<Pair<String, String>>()
        val callbacks = mutableListOf<(EmailChangeResult) -> Unit>()
        val reloadCallbacks = mutableListOf<(EmailReloadResult) -> Unit>()

        override fun requestVerification(
            expectedUid: String,
            normalizedEmail: String,
            onResult: (EmailChangeResult) -> Unit
        ) {
            requests++
            requested += expectedUid to normalizedEmail
            callbacks += onResult
            if (completeImmediately) onResult(result)
        }

        fun emit(result: EmailChangeResult) = callbacks.last()(result)

        override fun reloadConfirmedEmail(
            expectedUid: String,
            onResult: (EmailReloadResult) -> Unit
        ) {
            reloadCallbacks += onResult
            if (completeReloadImmediately) onResult(reloadResult)
        }

        fun emitReload(result: EmailReloadResult) = reloadCallbacks.last()(result)
    }

    private class FakeGateway(
        override var currentUid: String?
    ) : EmailChangeGateway {
        val verificationRequests = mutableListOf<String>()
        private var callback: ((EmailChangeResult) -> Unit)? = null
        private var reloadCallback: ((EmailReloadResult) -> Unit)? = null

        override fun verifyBeforeUpdateEmail(
            normalizedEmail: String,
            onResult: (EmailChangeResult) -> Unit
        ) {
            verificationRequests += normalizedEmail
            callback = onResult
        }

        fun emit(result: EmailChangeResult) = requireNotNull(callback)(result)

        override fun reloadCurrentUser(onResult: (EmailReloadResult) -> Unit) {
            reloadCallback = onResult
        }

        fun emitReload(result: EmailReloadResult) = requireNotNull(reloadCallback)(result)
    }

    private class FakeReconciler(
        private val succeeds: Boolean = true
    ) : ConfirmedEmailReconciler {
        val emails = mutableListOf<String>()

        override fun reconcile(
            expectedUid: String,
            confirmedEmail: String,
            onComplete: (Boolean) -> Unit
        ) {
            emails += confirmedEmail
            onComplete(succeeds)
        }
    }
}
