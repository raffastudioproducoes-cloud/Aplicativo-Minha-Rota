package com.raffastudioproducoes.minharota.data.local

import android.content.Context
import android.content.SharedPreferences
import com.raffastudioproducoes.minharota.domain.model.Caixinha
import com.raffastudioproducoes.minharota.domain.model.ContaDiaria
import com.raffastudioproducoes.minharota.domain.model.ContaFixa
import com.raffastudioproducoes.minharota.domain.model.Corrida
import com.raffastudioproducoes.minharota.domain.model.Divida
import com.raffastudioproducoes.minharota.domain.model.Movimentacao
import com.raffastudioproducoes.minharota.domain.model.Turno
import com.raffastudioproducoes.minharota.domain.model.Veiculo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("minha_rota_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // --- Caixinhas ---
    fun salvarCaixinhas(lista: List<Caixinha>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_CAIXINHAS, jsonString).apply()
    }

    fun obterCaixinhas(): List<Caixinha> {
        val jsonString = sharedPreferences.getString(KEY_CAIXINHAS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            val padrao = listOf(
                Caixinha(UUID.randomUUID().toString(), "Base de Tudo", "Custos essenciais", "🏠", "#820AD1", 40.0),
                Caixinha(UUID.randomUUID().toString(), "Manutenção", "Reserva para a moto", "🏍️", "#2ECC71", 30.0),
                Caixinha(UUID.randomUUID().toString(), "Lazer", "Diversão e descanso", "🎉", "#FFD700", 30.0)
            )
            salvarCaixinhas(padrao)
            padrao
        }
    }

    // --- Turnos ---
    fun salvarTurnos(lista: List<Turno>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_TURNOS, jsonString).apply()
    }

    fun obterTurnos(): List<Turno> {
        val jsonString = sharedPreferences.getString(KEY_TURNOS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun obterTodasCorridas(): List<Corrida> {
        return obterTurnos().flatMap { it.corridas }
    }

    // --- Contas Fixas ---
    fun salvarContas(lista: List<ContaFixa>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_CONTAS, jsonString).apply()
    }

    fun obterContas(): List<ContaFixa> {
        val jsonString = sharedPreferences.getString(KEY_CONTAS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // --- Movimentações ---
    fun salvarMovimentacoes(lista: List<Movimentacao>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_MOVIMENTACOES, jsonString).apply()
    }

    fun obterMovimentacoes(): List<Movimentacao> {
        val jsonString = sharedPreferences.getString(KEY_MOVIMENTACOES, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // --- Dívidas ---
    fun salvarDividas(lista: List<Divida>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_DIVIDAS, jsonString).apply()
    }

    fun obterDividas(): List<Divida> {
        val jsonString = sharedPreferences.getString(KEY_DIVIDAS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // --- Veículo ---
    fun salvarVeiculo(veiculo: Veiculo) {
        val jsonString = json.encodeToString(veiculo)
        sharedPreferences.edit().putString(KEY_VEICULO, jsonString).apply()
    }

    fun obterVeiculo(): Veiculo? {
        val jsonString = sharedPreferences.getString(KEY_VEICULO, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun jaGerouInsight(tipo: String, periodo: String, hojeStr: String): Boolean {
        val chave = "last_insight_${tipo}_$periodo"
        return sharedPreferences.getString(chave, "") == hojeStr
    }

    fun salvarInsightGerado(tipo: String, periodo: String, hojeStr: String) {
        val chave = "last_insight_${tipo}_$periodo"
        sharedPreferences.edit().putString(chave, hojeStr).apply()
    }

    // --- Perfil do Usuário ---
    fun salvarNomeUsuario(nome: String) {
        sharedPreferences.edit().putString(KEY_NOME_USUARIO, nome).apply()
    }

    fun obterNomeUsuario(): String {
        return sharedPreferences.getString(KEY_NOME_USUARIO, "Motorista") ?: "Motorista"
    }

    fun salvarEmail(email: String) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun obterEmail(): String {
        return sharedPreferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun salvarDataAniversario(data: String) {
        sharedPreferences.edit().putString(KEY_DATA_ANIVERSARIO, data).apply()
    }

    fun obterDataAniversario(): String {
        return sharedPreferences.getString(KEY_DATA_ANIVERSARIO, "") ?: ""
    }

    fun salvarFotoPerfilUrl(url: String) {
        sharedPreferences.edit().putString(KEY_FOTO_PERFIL, url).apply()
    }

    fun obterFotoPerfilUrl(): String {
        return sharedPreferences.getString(KEY_FOTO_PERFIL, "") ?: ""
    }

    fun salvarIsPro(isPro: Boolean) {
        sharedPreferences.edit().putBoolean("is_pro", isPro).apply()
    }

    fun obterIsPro(): Boolean {
        return sharedPreferences.getBoolean("is_pro", false)
    }

    // --- Plano e Vencimento ---
    fun salvarNomePlano(nome: String) {
        sharedPreferences.edit().putString(KEY_NOME_PLANO, nome).apply()
    }

    fun obterNomePlano(): String {
        return sharedPreferences.getString(KEY_NOME_PLANO, "Free") ?: "Free"
    }

    /** Salva a data de vencimento do plano no formato ISO (yyyy-MM-dd). */
    fun salvarDataVencimento(dataIso: String) {
        sharedPreferences.edit().putString(KEY_DATA_VENCIMENTO, dataIso).apply()
    }

    /** Retorna a data de vencimento do plano no formato ISO (yyyy-MM-dd), ou string vazia se não definida. */
    fun obterDataVencimento(): String {
        return sharedPreferences.getString(KEY_DATA_VENCIMENTO, "") ?: ""
    }

    // --- Dias de Folga ---
    fun salvarDiasFolga(dias: Set<Int>) {
        sharedPreferences.edit().putStringSet(KEY_DIAS_FOLGA, dias.map { it.toString() }.toSet()).apply()
    }

    fun obterDiasFolga(): Set<Int> {
        return sharedPreferences.getStringSet(KEY_DIAS_FOLGA, emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()
    }

    // --- Conta Diária ---
    fun salvarContasDiarias(lista: List<ContaDiaria>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_CONTAS_DIARIAS, jsonString).apply()
    }

    fun obterContasDiarias(): List<ContaDiaria> {
        val jsonString = sharedPreferences.getString(KEY_CONTAS_DIARIAS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // --- Meta Diária ---
    fun salvarMetaDiaria(valor: Double) {
        sharedPreferences.edit().putFloat(KEY_META_DIARIA, valor.toFloat()).apply()
    }

    fun obterMetaDiaria(): Double {
        return sharedPreferences.getFloat(KEY_META_DIARIA, 0.0f).toDouble()
    }

    // --- Faturamento MEI ---
    fun salvarFaturamentoBrutoAcumulado(valor: Double) {
        sharedPreferences.edit().putFloat(KEY_FATURAMENTO_MEI, valor.toFloat()).apply()
    }

    fun obterFaturamentoBrutoAcumulado(): Double {
        return sharedPreferences.getFloat(KEY_FATURAMENTO_MEI, 0.0f).toDouble()
    }

    // --- Garagem ---
    fun salvarKmAtual(km: Int) {
        sharedPreferences.edit().putInt(KEY_KM_ATUAL, km).apply()
    }

    fun obterKmAtual(): Int {
        return sharedPreferences.getInt(KEY_KM_ATUAL, 0)
    }

    fun salvarKmTotal(km: Int) {
        sharedPreferences.edit().putInt(KEY_KM_TOTAL, km).apply()
    }

    fun obterKmTotal(): Int {
        return sharedPreferences.getInt(KEY_KM_TOTAL, 0)
    }

    fun salvarManutencoes(lista: List<com.raffastudioproducoes.minharota.ui.screens.garagem.Manutencao>) {
        val jsonString = json.encodeToString(lista)
        sharedPreferences.edit().putString(KEY_MANUTENCOES, jsonString).apply()
    }

    fun obterManutencoes(): List<com.raffastudioproducoes.minharota.ui.screens.garagem.Manutencao> {
        val jsonString = sharedPreferences.getString(KEY_MANUTENCOES, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    companion object {
        private const val KEY_KM_ATUAL = "km_atual"
        private const val KEY_KM_TOTAL = "km_total_acumulado"
        private const val KEY_MANUTENCOES = "manutencoes"
        private const val KEY_FATURAMENTO_MEI = "faturamento_mei_acumulado"
        private const val KEY_CAIXINHAS = "caixinhas"
        private const val KEY_TURNOS = "turnos"
        private const val KEY_MOVIMENTACOES = "movimentacoes"
        private const val KEY_CONTAS = "contas_fixas"
        private const val KEY_DIVIDAS = "dividas"
        private const val KEY_VEICULO = "veiculo"
        private const val KEY_NOME_USUARIO = "nome_usuario"
        private const val KEY_EMAIL = "email"
        private const val KEY_DATA_ANIVERSARIO = "data_aniversario"
        private const val KEY_FOTO_PERFIL = "foto_perfil"
        private const val KEY_IS_PRO = "is_pro"
        private const val KEY_NOME_PLANO = "nome_plano"
        private const val KEY_DATA_VENCIMENTO = "data_vencimento_plano"
        private const val KEY_CONTAS_DIARIAS = "contas_diarias"
        private const val KEY_DIAS_FOLGA = "dias_folga"
        private const val KEY_META_DIARIA = "meta_diaria_persistente"
    }
}
