package com.raffastudioproducoes.minharota.auth

import com.raffastudioproducoes.minharota.repository.auth.AuthError
import com.raffastudioproducoes.minharota.repository.auth.AuthRepository
import com.raffastudioproducoes.minharota.repository.auth.AuthResult
import com.raffastudioproducoes.minharota.repository.auth.AuthUserSession
import com.raffastudioproducoes.minharota.repository.auth.RegistrationResult
import com.raffastudioproducoes.minharota.ui.screens.auth.AuthState
import com.raffastudioproducoes.minharota.ui.screens.auth.EmailAuthViewModel
import com.raffastudioproducoes.minharota.ui.screens.auth.allowsNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailAuthViewModelTest {

    @Test
    fun `login nao verificado vai para verificacao`() {
        val repository = FakeAuthRepository(loginResult = AuthResult.EmailVerificationRequired)
        val viewModel = EmailAuthViewModel(repository)

        viewModel.login("motorista@example.com", "123456")

        assertTrue(viewModel.authState.value is AuthState.EmailVerificationRequired)
        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `cadastro envia verificacao somente depois do perfil valido`() {
        val repository = FakeAuthRepository(completeRegistrationImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        repository.emitRegistration(RegistrationResult.AccountCreated)
        assertEquals(AuthState.Loading, viewModel.authState.value)
        repository.emitRegistration(RegistrationResult.ProfileUpdated)
        assertEquals(AuthState.Loading, viewModel.authState.value)
        repository.emitRegistration(RegistrationResult.VerificationSent)

        assertTrue(viewModel.authState.value is AuthState.EmailVerificationRequired)
    }

    @Test
    fun `falha no envio da verificacao nao recria conta`() {
        val repository = FakeAuthRepository(
            registrationResults = listOf(
                RegistrationResult.AccountCreated,
                RegistrationResult.ProfileUpdated,
                RegistrationResult.Failure(AuthError.EMAIL_VERIFICATION_FAILED)
            ),
            profileRetryResult = AuthResult.EmailVerificationRequired
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")
        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals(1, repository.registerCalls)
        assertEquals(1, repository.profileRetryCalls)
    }

    @Test
    fun `reload ainda nao verificado permanece na tela`() {
        val repository = FakeAuthRepository(reloadResult = AuthResult.EmailVerificationRequired)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startEmailVerification()

        viewModel.checkEmailVerification()

        assertTrue(viewModel.authState.value is AuthState.EmailVerificationRequired)
    }

    @Test
    fun `reload verificado reavalia nome valido para main`() {
        val repository = FakeAuthRepository(reloadResult = AuthResult.Success)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startEmailVerification()

        viewModel.checkEmailVerification()

        assertEquals(AuthState.Authenticated, viewModel.authState.value)
    }

    @Test
    fun `reload verificado sem nome direciona para conclusao`() {
        val repository = FakeAuthRepository(reloadResult = AuthResult.ProfileIncomplete)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startEmailVerification()

        viewModel.checkEmailVerification()

        assertEquals(AuthState.ProfileCompletionRequired, viewModel.authState.value)
    }

    @Test
    fun `troca de uid durante reload impede navegacao`() {
        val repository = FakeAuthRepository(completeReloadImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startEmailVerification()
        viewModel.checkEmailVerification()
        repository.currentUid = "user-b"

        repository.emitReload(AuthResult.Success)

        assertFalse(viewModel.authState.value.allowsNavigation())
        assertEquals(AuthState.Error(AuthError.SESSION_CHANGED), viewModel.authState.value)
    }

    @Test
    fun `currentUser nulo depois do reload impede navegacao`() {
        val repository = FakeAuthRepository(completeReloadImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startEmailVerification()
        viewModel.checkEmailVerification()
        repository.currentUid = null

        repository.emitReload(AuthResult.Success)

        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `callback atrasado de reload e ignorado depois do logout`() {
        val repository = FakeAuthRepository(completeReloadImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startEmailVerification()
        viewModel.checkEmailVerification()

        viewModel.logout()
        repository.emitReload(AuthResult.Success)

        assertEquals(AuthState.Idle, viewModel.authState.value)
        assertEquals(1, repository.signOutCalls)
    }

    @Test
    fun `reenvio duplicado durante loading e bloqueado`() {
        var now = 1_000L
        val repository = FakeAuthRepository(completeResendImmediately = false)
        val viewModel = EmailAuthViewModel(repository) { now }
        viewModel.startEmailVerification()
        now += 60_000L

        viewModel.resendVerificationEmail()
        viewModel.resendVerificationEmail()

        assertEquals(1, repository.resendCalls)
        assertEquals(AuthState.Loading, viewModel.authState.value)
    }

    @Test
    fun `cooldown impede reenvio imediato`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository) { 1_000L }
        viewModel.startEmailVerification()

        viewModel.resendVerificationEmail()

        assertEquals(0, repository.resendCalls)
        assertEquals(60L, viewModel.cooldownRemainingSeconds())
    }

    @Test
    fun `usuario inexistente e senha incorreta tem erro publico equivalente`() {
        val missingUser = EmailAuthViewModel(
            FakeAuthRepository(loginResult = AuthResult.Failure(AuthError.INVALID_CREDENTIALS))
        )
        val wrongPassword = EmailAuthViewModel(
            FakeAuthRepository(loginResult = AuthResult.Failure(AuthError.INVALID_CREDENTIALS))
        )

        missingUser.login("motorista@example.com", "123456")
        wrongPassword.login("motorista@example.com", "654321")

        assertEquals(missingUser.authState.value, wrongPassword.authState.value)
    }

    @Test
    fun `recuperacao usa uid da conta que criou a pendencia`() {
        val repository = FakeAuthRepository(
            currentUid = "user-a",
            registrationResults = listOf(
                RegistrationResult.AccountCreated,
                RegistrationResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
            )
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("  Rafa  ", "motorista@example.com", "123456", "123456")
        viewModel.completeProfile("Rafa")

        assertEquals(listOf("user-a" to "Rafa"), repository.retriedProfiles)
    }

    @Test
    fun `uid diferente impede atualizacao de perfil`() {
        val repository = FakeAuthRepository(currentUid = "user-a")
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()
        repository.currentUid = "user-b"

        viewModel.completeProfile("Rafa")

        assertEquals(0, repository.profileRetryCalls)
        assertEquals(AuthState.Error(AuthError.PROFILE_UPDATE_FAILED), viewModel.authState.value)
    }

    @Test
    fun `currentUser nulo impede atualizacao de perfil`() {
        val repository = FakeAuthRepository(currentUid = "user-a")
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()
        repository.currentUid = null

        viewModel.completeProfile("Rafa")

        assertEquals(0, repository.profileRetryCalls)
        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `troca de conta durante atualizacao nao autentica`() {
        val repository = FakeAuthRepository(currentUid = "user-a", completeRetryImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()
        viewModel.completeProfile("Rafa")
        repository.currentUid = "user-b"

        repository.emitRetry(AuthResult.Success)

        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `callback atrasado de sessao anterior e ignorado`() {
        val repository = FakeAuthRepository(currentUid = "user-a", completeRetryImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()
        viewModel.completeProfile("Rafa")
        viewModel.logout()
        repository.currentUid = "user-b"

        repository.emitRetry(AuthResult.Success)

        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `conclusao do perfil nao cria conta novamente`() {
        val repository = FakeAuthRepository(currentUid = "user-a")
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()

        viewModel.completeProfile("Rafa")

        assertEquals(0, repository.registerCalls)
        assertEquals(1, repository.profileRetryCalls)
    }

    @Test
    fun `conclusao autentica somente se uid continua igual`() {
        val repository = FakeAuthRepository(currentUid = "user-a", completeRetryImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()
        viewModel.completeProfile("Rafa")

        repository.emitRetry(AuthResult.Success)

        assertEquals(AuthState.Authenticated, viewModel.authState.value)
    }

    @Test
    fun `falha na conclusao mantem usuario fora da area principal`() {
        val repository = FakeAuthRepository(
            currentUid = "user-a",
            profileRetryResult = AuthResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
        )
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()

        viewModel.completeProfile("Rafa")

        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `submissoes duplicadas de conclusao ficam bloqueadas durante loading`() {
        val repository = FakeAuthRepository(currentUid = "user-a", completeRetryImmediately = false)
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()

        viewModel.completeProfile("Rafa")
        viewModel.completeProfile("Outro")

        assertEquals(1, repository.profileRetryCalls)
    }

    @Test
    fun `logout invalida recuperacao pendente`() {
        val repository = FakeAuthRepository(currentUid = "user-a")
        val viewModel = EmailAuthViewModel(repository)
        viewModel.startProfileCompletion()

        viewModel.logout()
        viewModel.completeProfile("Rafa")

        assertEquals(0, repository.profileRetryCalls)
        assertFalse(viewModel.isProfileRecoveryPending)
    }

    @Test
    fun `login bem sucedido autentica`() {
        val repository = FakeAuthRepository(loginResult = AuthResult.Success)
        val viewModel = EmailAuthViewModel(repository)

        viewModel.login("motorista@example.com", "123456")

        assertEquals(AuthState.Authenticated, viewModel.authState.value)
    }

    @Test
    fun `login recusado apresenta erro seguro`() {
        val repository = FakeAuthRepository(
            loginResult = AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.login("motorista@example.com", "123456")

        assertEquals(
            AuthState.Error(AuthError.INVALID_CREDENTIALS),
            viewModel.authState.value
        )
    }

    @Test
    fun `cadastro com perfil concluido aguarda verificacao de email`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals("EmailVerificationRequired", viewModel.authState.value::class.simpleName)
        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `cadastro recusado apresenta erro seguro`() {
        val repository = FakeAuthRepository(
            registrationResults = listOf(
                RegistrationResult.Failure(AuthError.REQUEST_FAILED)
            )
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals(
            AuthState.Error(AuthError.REQUEST_FAILED),
            viewModel.authState.value
        )
    }

    @Test
    fun `nome vazio impede cadastro`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("   ", "motorista@example.com", "123456", "123456")

        assertEquals(AuthState.Error(AuthError.REQUIRED_FIELDS), viewModel.authState.value)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `nome com espacos e normalizado`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("  Rafa Silva  ", "motorista@example.com", "123456", "123456")

        assertEquals("Rafa Silva", repository.registeredNames.single())
    }

    @Test
    fun `nome chega ao repositorio`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals(listOf("Rafa"), repository.registeredNames)
    }

    @Test
    fun `conta criada ainda nao autentica antes da atualizacao do perfil`() {
        val repository = FakeAuthRepository(completeRegistrationImmediately = false)
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")
        repository.emitRegistration(RegistrationResult.AccountCreated)

        assertEquals(AuthState.Loading, viewModel.authState.value)
        assertFalse(viewModel.authState.value.allowsNavigation())

        repository.emitRegistration(RegistrationResult.ProfileUpdated)
        repository.emitRegistration(RegistrationResult.VerificationSent)
        assertEquals("EmailVerificationRequired", viewModel.authState.value::class.simpleName)
        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `falha na atualizacao do perfil nao permite navegacao`() {
        val repository = FakeAuthRepository(
            registrationResults = listOf(
                RegistrationResult.AccountCreated,
                RegistrationResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
            )
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals(
            AuthState.Error(AuthError.PROFILE_UPDATE_FAILED),
            viewModel.authState.value
        )
        assertFalse(viewModel.authState.value.allowsNavigation())
    }

    @Test
    fun `recuperacao repete somente atualizacao do perfil`() {
        val repository = FakeAuthRepository(
            registrationResults = listOf(
                RegistrationResult.AccountCreated,
                RegistrationResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
            ),
            profileRetryResult = AuthResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")
        viewModel.register("Outro nome", "outro@example.com", "654321", "654321")

        assertEquals(1, repository.registerCalls)
        assertEquals(1, repository.profileRetryCalls)
        assertEquals(listOf("Rafa"), repository.retriedProfileNames)
    }

    @Test
    fun `segunda tentativa nao recria a conta`() {
        val repository = FakeAuthRepository(
            registrationResults = listOf(
                RegistrationResult.AccountCreated,
                RegistrationResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
            )
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")
        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals(1, repository.registerCalls)
    }

    @Test
    fun `sucesso da recuperacao autentica`() {
        val repository = FakeAuthRepository(
            registrationResults = listOf(
                RegistrationResult.AccountCreated,
                RegistrationResult.Failure(AuthError.PROFILE_UPDATE_FAILED)
            ),
            profileRetryResult = AuthResult.Success
        )
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")
        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")

        assertEquals(AuthState.Authenticated, viewModel.authState.value)
        assertEquals(1, repository.registerCalls)
        assertEquals(1, repository.profileRetryCalls)
    }

    @Test
    fun `email invalido impede autenticacao`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.login("email-invalido", "123456")

        assertEquals(AuthState.Error(AuthError.INVALID_EMAIL), viewModel.authState.value)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `senha insuficiente impede autenticacao`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.login("motorista@example.com", "12345")

        assertEquals(AuthState.Error(AuthError.WEAK_PASSWORD), viewModel.authState.value)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `confirmacao de senha diferente impede cadastro`() {
        val repository = FakeAuthRepository()
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "654321")

        assertEquals(
            AuthState.Error(AuthError.PASSWORDS_DO_NOT_MATCH),
            viewModel.authState.value
        )
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `submissao duplicada de cadastro fica bloqueada durante loading`() {
        val repository = FakeAuthRepository(completeRegistrationImmediately = false)
        val viewModel = EmailAuthViewModel(repository)

        viewModel.register("Rafa", "motorista@example.com", "123456", "123456")
        viewModel.register("Outro", "outro@example.com", "654321", "654321")

        assertEquals(AuthState.Loading, viewModel.authState.value)
        assertEquals(1, repository.registerCalls)
        assertFalse(viewModel.isSubmitEnabled)
    }

    @Test
    fun `novo login fica bloqueado durante loading`() {
        val repository = FakeAuthRepository(completeLoginImmediately = false)
        val viewModel = EmailAuthViewModel(repository)

        viewModel.login("motorista@example.com", "123456")
        viewModel.login("outro@example.com", "654321")

        assertEquals(AuthState.Loading, viewModel.authState.value)
        assertEquals(1, repository.loginCalls)
        assertFalse(viewModel.isSubmitEnabled)
    }

    @Test
    fun `navegacao somente e permitida no estado autenticado`() {
        assertFalse(AuthState.Idle.allowsNavigation())
        assertFalse(AuthState.Loading.allowsNavigation())
        assertFalse(AuthState.Error(AuthError.UNKNOWN).allowsNavigation())
        assertFalse(AuthState.EmailVerificationRequired(60_000L).allowsNavigation())
        assertFalse(AuthState.ProfileCompletionRequired.allowsNavigation())
        assertTrue(AuthState.Authenticated.allowsNavigation())
    }

    private class FakeAuthRepository(
        private val loginResult: AuthResult = AuthResult.Success,
        private val registrationResults: List<RegistrationResult> = listOf(
            RegistrationResult.AccountCreated,
            RegistrationResult.ProfileUpdated,
            RegistrationResult.VerificationSent
        ),
        private val profileRetryResult: AuthResult = AuthResult.Success,
        private val completeLoginImmediately: Boolean = true,
        private val completeRegistrationImmediately: Boolean = true,
        var currentUid: String? = "user-a",
        private val completeRetryImmediately: Boolean = true,
        private val reloadResult: AuthResult = AuthResult.EmailVerificationRequired,
        private val completeReloadImmediately: Boolean = true,
        private val completeResendImmediately: Boolean = true
    ) : AuthRepository {
        override val currentUserUid: String?
            get() = currentUid
        override val currentUserSession: AuthUserSession?
            get() = currentUid?.let {
                AuthUserSession(it, "Rafa", false, setOf("password"))
            }
        var loginCalls = 0
            private set
        var registerCalls = 0
            private set
        var profileRetryCalls = 0
            private set
        var resendCalls = 0
            private set
        var signOutCalls = 0
            private set
        val registeredNames = mutableListOf<String>()
        val retriedProfileNames = mutableListOf<String>()
        val retriedProfiles = mutableListOf<Pair<String, String>>()
        private var registrationCallback: ((RegistrationResult) -> Unit)? = null
        private var retryCallback: ((AuthResult) -> Unit)? = null
        private var reloadCallback: ((AuthResult) -> Unit)? = null
        private var resendCallback: ((AuthResult) -> Unit)? = null

        override fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
            loginCalls++
            if (completeLoginImmediately) onResult(loginResult)
        }

        override fun register(
            name: String,
            email: String,
            password: String,
            onResult: (RegistrationResult) -> Unit
        ) {
            registerCalls++
            registeredNames += name
            registrationCallback = onResult
            if (completeRegistrationImmediately) {
                registrationResults.forEach(onResult)
            }
        }

        override fun retryProfileUpdate(uid: String, name: String, onResult: (AuthResult) -> Unit) {
            profileRetryCalls++
            retriedProfileNames += name
            retriedProfiles += uid to name
            retryCallback = onResult
            if (completeRetryImmediately) onResult(profileRetryResult)
        }

        override fun resendEmailVerification(uid: String, onResult: (AuthResult) -> Unit) {
            resendCalls++
            resendCallback = onResult
            if (completeResendImmediately) onResult(AuthResult.EmailVerificationRequired)
        }

        override fun reloadSession(uid: String, onResult: (AuthResult) -> Unit) {
            reloadCallback = onResult
            if (completeReloadImmediately) onResult(reloadResult)
        }

        override fun signOut() {
            signOutCalls++
            currentUid = null
        }

        fun emitRegistration(result: RegistrationResult) {
            requireNotNull(registrationCallback)(result)
        }

        fun emitRetry(result: AuthResult) {
            requireNotNull(retryCallback)(result)
        }

        fun emitReload(result: AuthResult) {
            requireNotNull(reloadCallback)(result)
        }
    }
}
