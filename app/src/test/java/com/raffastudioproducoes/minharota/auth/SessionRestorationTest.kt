package com.raffastudioproducoes.minharota.auth

import com.raffastudioproducoes.minharota.repository.auth.SessionDestination
import com.raffastudioproducoes.minharota.repository.auth.restoreSessionDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SessionRestorationTest {
    @Test
    fun `usuario de email nao verificado nao entra em main`() {
        val destination = restoreForTest(
            uid = "user-a",
            displayName = "Rafa",
            isEmailVerified = false,
            providerIds = setOf("password")
        )

        assertNotEquals(SessionDestination.MAIN, destination)
    }

    @Test
    fun `usuario de email nao verificado vai para verificacao`() {
        assertEquals(
            "VERIFY_EMAIL",
            restoreForTest("user-a", "Rafa", false, setOf("password")).name
        )
    }

    @Test
    fun `usuario de email verificado com nome valido vai para main`() {
        assertEquals(
            SessionDestination.MAIN,
            restoreForTest("user-a", "Rafa", true, setOf("password"))
        )
    }

    @Test
    fun `usuario de email verificado sem nome conclui perfil`() {
        assertEquals(
            SessionDestination.COMPLETE_PROFILE,
            restoreForTest("user-a", "  ", true, setOf("password"))
        )
    }

    @Test
    fun `google com flag nao verificada nao libera main silenciosamente`() {
        assertEquals(
            SessionDestination.VERIFY_EMAIL,
            restoreForTest("user-g", "Rafa", false, setOf("google.com"))
        )
    }

    @Test
    fun `somente password verificado segue fluxo normal`() {
        assertEquals(
            SessionDestination.MAIN,
            restoreForTest("user-p", "Rafa", true, setOf("password"))
        )
    }

    @Test
    fun `somente google verificado segue fluxo normal`() {
        assertEquals(
            SessionDestination.MAIN,
            restoreForTest("user-g", "Rafa", true, setOf("google.com"))
        )
    }

    @Test
    fun `password e google vinculados nao verificados nao liberam main`() {
        assertEquals(
            SessionDestination.VERIFY_EMAIL,
            restoreForTest("user-l", "Rafa", false, setOf("password", "google.com"))
        )
    }

    @Test
    fun `password e google vinculados verificados seguem fluxo normal`() {
        assertEquals(
            SessionDestination.MAIN,
            restoreForTest("user-l", "Rafa", true, setOf("password", "google.com"))
        )
    }

    @Test
    fun `conta verificada sem displayName conclui perfil`() {
        assertEquals(
            SessionDestination.COMPLETE_PROFILE,
            restoreForTest("user-a", null, true, setOf("password", "google.com"))
        )
    }

    @Test
    fun `nenhuma combinacao nao verificada recebe main`() {
        val providerCombinations = listOf(
            setOf("password"),
            setOf("google.com"),
            setOf("password", "google.com")
        )

        providerCombinations.forEach { providers ->
            assertNotEquals(
                SessionDestination.MAIN,
                restoreForTest("user-a", "Rafa", false, providers)
            )
        }
    }

    @Test
    fun `displayName vazio na inicializacao nao abre area principal`() {
        assertEquals(SessionDestination.COMPLETE_PROFILE, restoreSessionDestination("user-a", "   "))
    }

    @Test
    fun `displayName nulo direciona para conclusao do perfil`() {
        assertEquals(SessionDestination.COMPLETE_PROFILE, restoreSessionDestination("user-a", null))
    }

    @Test
    fun `displayName valido permite fluxo normal`() {
        assertEquals(SessionDestination.MAIN, restoreSessionDestination("user-a", "  Rafa Silva  "))
    }

    @Test
    fun `usuario ausente direciona para autenticacao`() {
        assertEquals(SessionDestination.LOGIN, restoreSessionDestination(null, null))
    }

    private fun restoreForTest(
        uid: String?,
        displayName: String?,
        isEmailVerified: Boolean,
        providerIds: Set<String>
    ): SessionDestination = restoreSessionDestination(
        uid = uid,
        displayName = displayName,
        isEmailVerified = isEmailVerified,
        providerIds = providerIds
    )
}
