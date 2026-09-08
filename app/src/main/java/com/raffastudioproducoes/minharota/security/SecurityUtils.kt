package com.raffastudioproducoes.minharota.security

import android.text.Html
import android.util.Log

object SecurityUtils {

    private val TAG = "SecurityUtils"

    // HTML entities que precisam ser escaped
    private val HTML_ESCAPE_MAP = mapOf(
        "&" to "&amp;",
        "<" to "&lt;",
        ">" to "&gt;",
        "\"" to "&quot;",
        "'" to "&#x27;",
        "/" to "&#x2F;"
    )

    /**
     * Sanitiza string removendo/escapando HTML/JavaScript
     * @param input String potencialmente maliciosa
     * @return String sanitizada segura para exibição
     */
    fun sanitizeHtml(input: String?): String {
        if (input == null || input.isEmpty()) return ""

        // Remove script tags and their content
        var sanitized = input.replace(
            Regex("<script[^>]*>.*?</script>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
            ""
        )

        // Remove other dangerous tags
        sanitized = sanitized.replace(Regex("<iframe[^>]*>.*?</iframe>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")
        sanitized = sanitized.replace(Regex("<object[^>]*>.*?</object>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")
        sanitized = sanitized.replace(Regex("<embed[^>]*>", RegexOption.IGNORE_CASE), "")

        // Remove event handlers (onclick, onerror, etc)
        sanitized = sanitized.replace(Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE), "")

        // Escape remaining HTML entities
        sanitized = escapeHtml(sanitized)

        if (sanitized != input) {
            Log.w(TAG, "XSS attempt detected and sanitized")
        }

        return sanitized
    }

    /**
     * Escapa caracteres HTML especiais
     */
    fun escapeHtml(input: String?): String {
        if (input == null || input.isEmpty()) return ""

        var result: String = input
        HTML_ESCAPE_MAP.forEach { (char, entity) ->
            result = result.replace(char, entity)
        }
        return result
    }

    /**
     * Valida e sanitiza entrada de usuário
     * @param input String do usuário
     * @param maxLength Comprimento máximo permitido
     * @return String sanitizada ou null se inválida
     */
    fun validateAndSanitizeInput(
        input: String?,
        maxLength: Int = 255,
        allowHtml: Boolean = false
    ): String? {
        if (input == null || input.isEmpty()) return null

        // Validar comprimento
        if (input.length > maxLength) {
            Log.w(TAG, "Input exceeds max length: ${input.length} > $maxLength")
            return null
        }

        // Sanitizar se HTML não é permitido
        val sanitized = if (allowHtml) input else sanitizeHtml(input)
        if (sanitized == null || sanitized.isEmpty()) return null

        // Validar caracteres suspeitos
        if (containsSuspiciousPatterns(sanitized)) {
            Log.w(TAG, "Input contains suspicious patterns")
            return null
        }

        return sanitized
    }

    /**
     * Detecta padrões suspeitos comuns
     */
    private fun containsSuspiciousPatterns(input: String?): Boolean {
        if (input == null) return false

        val suspiciousPatterns = listOf(
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("data:text/html", RegexOption.IGNORE_CASE),
            Regex("vbscript:", RegexOption.IGNORE_CASE),
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("</script>", RegexOption.IGNORE_CASE),
            Regex("onerror\\s*=", RegexOption.IGNORE_CASE),
            Regex("onclick\\s*=", RegexOption.IGNORE_CASE),
            Regex("onload\\s*=", RegexOption.IGNORE_CASE),
            Regex("<iframe", RegexOption.IGNORE_CASE),
            Regex("<object", RegexOption.IGNORE_CASE)
        )

        return suspiciousPatterns.any { it.containsMatchIn(input) }
    }

    /**
     * Valida email com padrão seguro
     */
    fun validateEmail(email: String?): Boolean {
        if (email == null || email.isEmpty()) return false

        val emailPattern = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )

        return emailPattern.matches(email)
    }

    /**
     * Valida nome com caracteres permitidos apenas
     */
    fun validateName(name: String?): Boolean {
        if (name == null || name.isEmpty()) return false
        if (name.length > 255) return false

        // Permitir letras, números, espaços, hífens, apóstrofos
        val namePattern = Regex("^[a-zA-Z0-9\\s\\-'àáâãäåèéêëìíîïðòóôõöùúûüýÿçñ]+$")

        return namePattern.matches(name)
    }
}
