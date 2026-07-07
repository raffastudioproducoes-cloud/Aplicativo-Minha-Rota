package com.raffastudioproducoes.minharota.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.raffastudioproducoes.minharota.BuildConfig
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel centralizado para gerenciar chamadas ao Gemini AI
 * 
 * Responsável por:
 * 1. Gerar insights financeiros para HojeScreen
 * 2. Gerar dicas de manutenção para GaragemScreen
 * 3. Gerar estratégias financeiras para ContasScreen
 * 
 * Apenas funciona se o usuário é PRO
 */
class GeminiAiViewModel : ViewModel() {

    private val _hojeInsight = MutableStateFlow("")
    val hojeInsight: StateFlow<String> = _hojeInsight.asStateFlow()

    private val _hojeIsLoading = MutableStateFlow(false)
    val hojeIsLoading: StateFlow<Boolean> = _hojeIsLoading.asStateFlow()

    private val _garagemInsight = MutableStateFlow("")
    val garagemInsight: StateFlow<String> = _garagemInsight.asStateFlow()

    private val _garagemIsLoading = MutableStateFlow(false)
    val garagemIsLoading: StateFlow<Boolean> = _garagemIsLoading.asStateFlow()

    private val _contasInsight = MutableStateFlow("")
    val contasInsight: StateFlow<String> = _contasInsight.asStateFlow()

    private val _contasIsLoading = MutableStateFlow(false)
    val contasIsLoading: StateFlow<Boolean> = _contasIsLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Instância do GenerativeModel do Gemini
    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * HOJE SCREEN: Gera insight financeiro baseado em ganhos/horas
     * 
     * Prompt: "Atue como um mentor financeiro para motoristas. 
     * Dados: [Ganhos/Horas]. 
     * Forneça uma dica curta e amigável de 2 linhas para maximizar lucros. Sem formatação."
     */
    fun gerarInsightHoje(
        context: Context,
        ganhoBruto: Double,
        horasTrabalhadas: String,
        ganhoLiquido: Double
    ) {
        viewModelScope.launch {
            try {
                val prefs = SharedPreferencesManager(context)
                if (!prefs.obterIsPro()) {
                    Log.d("GeminiAI", "Usuário FREE - Insight não gerado")
                    return@launch
                }

                _hojeIsLoading.value = true
                _errorMessage.value = null

                val prompt = """
                    Atue como um mentor financeiro para motoristas e entregadores.
                    
                    Dados do turno:
                    - Ganho Bruto: R$ $ganhoBruto
                    - Horas Trabalhadas: $horasTrabalhadas
                    - Ganho Líquido: R$ $ganhoLiquido
                    
                    Forneça UMA dica curta e amigável de MÁXIMO 2 linhas para maximizar lucros.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    Exemplo: "Seus ganhos estão bons! Continue focando em horários de pico para aumentar ainda mais."
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val insight = response.text?.trim() ?: "Não foi possível gerar insight"

                _hojeInsight.value = insight
                Log.d("GeminiAI", "Insight Hoje gerado: $insight")

            } catch (e: Exception) {
                Log.e("GeminiAI", "Erro ao gerar insight Hoje", e)
                _errorMessage.value = "Erro ao gerar insight: ${e.message}"
                _hojeInsight.value = ""
            } finally {
                _hojeIsLoading.value = false
            }
        }
    }

    /**
     * GARAGEM SCREEN: Gera dica preditiva sobre manutenção
     * 
     * Prompt: "Atue como mecânico. KM Total: [KM]. Próximas manutenções: [Manutenções]. 
     * Dê uma dica preditiva de 2 linhas sobre desgaste de peças."
     */
    fun gerarInsightGaragem(
        context: Context,
        kmTotal: Int,
        proximasManutencoes: String
    ) {
        viewModelScope.launch {
            try {
                val prefs = SharedPreferencesManager(context)
                if (!prefs.obterIsPro()) {
                    Log.d("GeminiAI", "Usuário FREE - Insight Garagem não gerado")
                    return@launch
                }

                _garagemIsLoading.value = true
                _errorMessage.value = null

                val prompt = """
                    Atue como um mecânico experiente em motos e veículos de entrega.
                    
                    Dados do veículo:
                    - KM Total Acumulado: $kmTotal km
                    - Próximas Manutenções Previstas: $proximasManutencoes
                    
                    Dê UMA dica preditiva de MÁXIMO 2 linhas sobre desgaste potencial de peças.
                    Foque em prevenção e economia.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    Exemplo: "Com esse quilometragem, fique atento ao óleo do motor. Preventivo agora economiza dinheiro depois."
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val insight = response.text?.trim() ?: "Não foi possível gerar insight"

                _garagemInsight.value = insight
                Log.d("GeminiAI", "Insight Garagem gerado: $insight")

            } catch (e: Exception) {
                Log.e("GeminiAI", "Erro ao gerar insight Garagem", e)
                _errorMessage.value = "Erro ao gerar insight: ${e.message}"
                _garagemInsight.value = ""
            } finally {
                _garagemIsLoading.value = false
            }
        }
    }

    /**
     * CONTAS SCREEN: Gera estratégia de quitação de dívidas
     * 
     * Prompt: "Atue como estrategista financeiro. Dívidas: [Dívidas]. 
     * Dê um conselho curto de 2 linhas sobre como quitar ou evitar juros (efeito bola de neve)."
     */
    fun gerarInsightContas(
        context: Context,
        dividasInfo: String
    ) {
        viewModelScope.launch {
            try {
                val prefs = SharedPreferencesManager(context)
                if (!prefs.obterIsPro()) {
                    Log.d("GeminiAI", "Usuário FREE - Insight Contas não gerado")
                    return@launch
                }

                _contasIsLoading.value = true
                _errorMessage.value = null

                val prompt = """
                    Atue como um estrategista financeiro especializado em educação financeira para motoristas.
                    
                    Situação de Dívidas:
                    $dividasInfo
                    
                    Dê UM conselho curto de MÁXIMO 2 linhas sobre como quitar dívidas ou evitar o efeito bola de neve de juros.
                    Seja prático e motivador.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    Exemplo: "Priorize a dívida com maior taxa de juros. Isso quebra o ciclo e libera cash flow mais rápido."
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val insight = response.text?.trim() ?: "Não foi possível gerar insight"

                _contasInsight.value = insight
                Log.d("GeminiAI", "Insight Contas gerado: $insight")

            } catch (e: Exception) {
                Log.e("GeminiAI", "Erro ao gerar insight Contas", e)
                _errorMessage.value = "Erro ao gerar insight: ${e.message}"
                _contasInsight.value = ""
            } finally {
                _contasIsLoading.value = false
            }
        }
    }

    /**
     * Limpa todos os insights (útil ao sair das telas)
     */
    fun limparInsights() {
        _hojeInsight.value = ""
        _garagemInsight.value = ""
        _contasInsight.value = ""
        _hojeIsLoading.value = false
        _garagemIsLoading.value = false
        _contasIsLoading.value = false
        _errorMessage.value = null
    }
}
