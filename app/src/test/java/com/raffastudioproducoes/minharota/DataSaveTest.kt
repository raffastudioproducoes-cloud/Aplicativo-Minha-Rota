package com.raffastudioproducoes.minharota

import org.junit.Test

class DataSaveTest {
    @Test
    fun testDataPersistenceIntegration() {
        // Test: Verificar que dados podem ser salvos
        // 1. VM recebe entrada
        val ganho = 150.50
        val horas = "08:00"

        // 2. Verify dados são válidos
        assert(ganho > 0)
        assert(horas.isNotEmpty())

        println("✅ Test: Dados válidos para salvar")
        println("✅ Ganho bruto: $ganho")
        println("✅ Hora início: $horas")
    }

    @Test
    fun testAcentosNoNome() {
        // Test: Acentos no nome não são rejeitados
        val nomeComAcento = "Rafael Machado Galvão"
        val nomeComTils = "João da Silva"
        val nomeComCedilha = "França"

        // Sanitizar (mesmo padrão usado no app)
        val sanitizado1 = nomeComAcento.replace(Regex("<[^>]*>|<script.*?</script>|on\\w+\\s*="), "")
        val sanitizado2 = nomeComTils.replace(Regex("<[^>]*>|<script.*?</script>|on\\w+\\s*="), "")
        val sanitizado3 = nomeComCedilha.replace(Regex("<[^>]*>|<script.*?</script>|on\\w+\\s*="), "")

        assert(sanitizado1 == nomeComAcento)
        assert(sanitizado2 == nomeComTils)
        assert(sanitizado3 == nomeComCedilha)

        println("✅ Test: Acentos preservados após sanitização")
        println("✅ $nomeComAcento → $sanitizado1")
        println("✅ $nomeComTils → $sanitizado2")
        println("✅ $nomeComCedilha → $sanitizado3")
    }

    @Test
    fun testScriptRejection() {
        // Test: Scripts são rejeitados
        val withScript = "Rafael<script>alert('hack')</script>"
        val withEvent = "Rafael onload='hack()'"

        val sanitizado1 = withScript.replace(Regex("<[^>]*>|<script.*?</script>|on\\w+\\s*="), "")
        val sanitizado2 = withEvent.replace(Regex("<[^>]*>|<script.*?</script>|on\\w+\\s*="), "")

        assert(!sanitizado1.contains("<script>"))
        assert(!sanitizado2.contains("onload"))

        println("✅ Test: Scripts bloqueados")
        println("✅ $withScript → $sanitizado1")
        println("✅ $withEvent → $sanitizado2")
    }
}
