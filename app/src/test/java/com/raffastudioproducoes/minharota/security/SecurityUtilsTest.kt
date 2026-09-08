package com.raffastudioproducoes.minharota.security

import org.junit.Assert.*
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun sanitizeHtml_removesScriptTags() {
        val input = "<script>alert('XSS')</script>Hello"
        val result = SecurityUtils.sanitizeHtml(input)
        assertFalse(result.contains("<script"))
        assertFalse(result.contains("</script>"))
        assertTrue(result.contains("Hello"))
    }

    @Test
    fun sanitizeHtml_removesEventHandlers() {
        val input = "<img src=x onerror=\"alert('XSS')\">"
        val result = SecurityUtils.sanitizeHtml(input)
        assertFalse(result.contains("onerror"))
    }

    @Test
    fun sanitizeHtml_escapesHtmlEntities() {
        val input = "<b>Bold</b>"
        val result = SecurityUtils.sanitizeHtml(input)
        assertTrue(result.contains("&lt;"))
        assertTrue(result.contains("&gt;"))
    }

    @Test
    fun sanitizeHtml_removesIframeTags() {
        val input = "<iframe src=\"http://evil.com\"></iframe>"
        val result = SecurityUtils.sanitizeHtml(input)
        assertFalse(result.contains("<iframe"))
    }

    @Test
    fun sanitizeHtml_removesObjectTags() {
        val input = "<object data=\"http://evil.com\"></object>"
        val result = SecurityUtils.sanitizeHtml(input)
        assertFalse(result.contains("<object"))
    }

    @Test
    fun escapeHtml_escapesAmpersand() {
        val input = "A & B"
        val result = SecurityUtils.escapeHtml(input)
        assertEquals("A &amp; B", result)
    }

    @Test
    fun escapeHtml_escapesQuotes() {
        val input = "He said \"Hello\""
        val result = SecurityUtils.escapeHtml(input)
        assertTrue(result.contains("&quot;"))
    }

    @Test
    fun validateAndSanitizeInput_rejectsLongInput() {
        val longInput = "a".repeat(300)
        val result = SecurityUtils.validateAndSanitizeInput(longInput, maxLength = 255)
        assertNull(result)
    }

    @Test
    fun validateAndSanitizeInput_allowsValidInput() {
        val input = "John Doe"
        val result = SecurityUtils.validateAndSanitizeInput(input)
        assertNotNull(result)
        assertEquals(input, result)
    }

    @Test
    fun validateAndSanitizeInput_rejectsScriptPatterns() {
        val input = "Hello<script>alert('xss')</script>"
        val result = SecurityUtils.validateAndSanitizeInput(input)
        assertNull(result)
    }

    @Test
    fun validateEmail_acceptsValidEmail() {
        assertTrue(SecurityUtils.validateEmail("user@example.com"))
        assertTrue(SecurityUtils.validateEmail("john.doe@domain.co.uk"))
    }

    @Test
    fun validateEmail_rejectsInvalidEmail() {
        assertFalse(SecurityUtils.validateEmail("invalid"))
        assertFalse(SecurityUtils.validateEmail("user@"))
        assertFalse(SecurityUtils.validateEmail("@example.com"))
    }

    @Test
    fun validateName_acceptsValidName() {
        assertTrue(SecurityUtils.validateName("João Silva"))
        assertTrue(SecurityUtils.validateName("Mary-Jane"))
        assertTrue(SecurityUtils.validateName("O'Brien"))
    }

    @Test
    fun validateName_rejectsInvalidName() {
        assertFalse(SecurityUtils.validateName("<script>"))
        assertFalse(SecurityUtils.validateName("a".repeat(300)))
        assertFalse(SecurityUtils.validateName("test@domain.com"))
    }

    @Test
    fun containsSuspiciousPatterns_detectsJavaScript() {
        val patterns = listOf(
            "javascript:alert('xss')",
            "vbscript:msgbox('xss')",
            "data:text/html,<script>alert('xss')</script>"
        )

        patterns.forEach {
            // Method is private, so we test through sanitizeHtml
            val result = SecurityUtils.sanitizeHtml(it)
            // Should detect and sanitize
            assertFalse(result.contains("javascript:"))
        }
    }
}
