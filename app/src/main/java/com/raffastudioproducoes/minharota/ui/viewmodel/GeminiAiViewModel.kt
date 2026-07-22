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
import java.util.Calendar

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

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    private fun obterPeriodoAtual(): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hora) {
            in 5..11 -> "MANHA"
            in 12..17 -> "TARDE"
            else -> "NOITE"
        }
    }

    fun gerarInsightHoje(
        context: Context,
        ganhoBruto: Double,
        horasTrabalhadas: String,
        ganhoLiquido: Double
    ) {
        viewModelScope.launch {
            try {
                val prefs = SharedPreferencesManager(context)
                if (!prefs.obterIsPro()) return@launch

                val periodo = obterPeriodoAtual()
                val hojeStr = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toString()

                if (prefs.jaGerouInsight(
                        "hoje",
                        periodo,
                        hojeStr
                    ) && _hojeInsight.value.isNotBlank()
                ) return@launch

                _hojeIsLoading.value = true
                _errorMessage.value = null

                val prompt = if (ganhoBruto <= 0.0 && horasTrabalhadas == "00:00") {
                    """
                    Atue como um mentor motivacional para motoristas e entregadores.
                    O motorista ainda não registrou dados de ganhos hoje.
                    Dê uma mensagem motivacional contagiante de MÁXIMO 6 linhas incentivando-o a ligar o aplicativo e ir para a rua com foco e segurança.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    """.trimIndent()
                } else {
                    """
                    Atue como um mentor financeiro para motoristas e entregadores.
                    Dados do turno atual:
                    - Ganho Bruto: R$ $ganhoBruto
                    - Horas Trabalhadas: $horasTrabalhadas
                    - Ganho Líquido: R$ $ganhoLiquido
                    
                    Forneça UMA dica prática de MÁXIMO 6 linhas para maximizar lucros ou gerenciar o turno.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    """.trimIndent()
                }

                val response = generativeModel.generateContent(prompt)
                val insight = response.text?.trim()
                    ?: "Mantenha o foco e tenha um excelente turno de trabalho!"

                _hojeInsight.value = insight
                prefs.salvarInsightGerado("hoje", periodo, hojeStr)

            } catch (e: Exception) {
                Log.e("GeminiAI", "Erro ao gerar insight Hoje", e)
                _hojeInsight.value = "Foco na direção e bons ganhos hoje!"
            } finally {
                _hojeIsLoading.value = false
            }
        }
    }

    fun gerarInsightGaragem(
        context: Context,
        kmTotal: Int,
        proximasManutencoes: String
    ) {
        viewModelScope.launch {
            try {
                val prefs = SharedPreferencesManager(context)
                if (!prefs.obterIsPro()) return@launch

                val periodo = obterPeriodoAtual()
                val hojeStr = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toString()

                if (prefs.jaGerouInsight(
                        "garagem",
                        periodo,
                        hojeStr
                    ) && _garagemInsight.value.isNotBlank()
                ) return@launch

                _garagemIsLoading.value = true
                _errorMessage.value = null

                val prompt = if (kmTotal <= 0 && proximasManutencoes.isBlank()) {
                    """
                    Atue como um mecânico experiente e parceiro de motoristas.
                    O motorista ainda não cadastrou a quilometragem ou manutenções do veículo.
                    Dê uma mensagem motivacional de MÁXIMO 6 linhas lembrando da importância de cuidar da moto ou carro como ferramenta essencial de trabalho.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    """.trimIndent()
                } else {
                    """
                    Atue como um mecânico experiente em veículos de aplicativo.
                    Dados do veículo:
                    - KM Total: $kmTotal km
                    - Próximas Manutenções: $proximasManutencoes
                    
                    Dê UMA dica preditiva e preventiva de MÁXIMO 6 linhas focada em economia de peças e segurança.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    """.trimIndent()
                }

                val response = generativeModel.generateContent(prompt)
                val insight = response.text?.trim()
                    ?: "Faça uma revisão rápida nos pneus e óleo regularmente."

                _garagemInsight.value = insight
                prefs.salvarInsightGerado("garagem", periodo, hojeStr)

            } catch (e: Exception) {
                Log.e("GeminiAI", "Erro ao gerar insight Garagem", e)
                _garagemInsight.value =
                    "Mantenha a manutenção preventiva em dia para evitar surpresas na rua."
            } finally {
                _garagemIsLoading.value = false
            }
        }
    }

    fun gerarInsightContas(
        context: Context,
        dividasInfo: String
    ) {
        viewModelScope.launch {
            try {
                val prefs = SharedPreferencesManager(context)
                if (!prefs.obterIsPro()) return@launch

                val periodo = obterPeriodoAtual()
                val hojeStr = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toString()

                if (prefs.jaGerouInsight(
                        "contas",
                        periodo,
                        hojeStr
                    ) && _contasInsight.value.isNotBlank()
                ) return@launch

                _contasIsLoading.value = true
                _errorMessage.value = null

                val prompt = if (dividasInfo.isBlank()) {
                    """
                    Atue como um estrategista financeiro motivacional para motoristas.
                    O motorista não possui dívidas cadastradas no momento.
                    Dê uma mensagem de incentivo e parabéns de MÁXIMO 6 linhas focada em inteligência financeira, reserva de emergência e investimentos futuros.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    """.trimIndent()
                } else {
                    """
                    Atue como um estrategista financeiro para motoristas.
                    Situação de dívidas cadastradas:
                    $dividasInfo
                    
                    Dê UM conselho prático de MÁXIMO 6 linhas sobre como organizar os pagamentos ou escapar de juros altos.
                    Responda SEM formatação, SEM emojis, SEM markdown.
                    """.trimIndent()
                }

                val response = generativeModel.generateContent(prompt)
                val insight = response.text?.trim()
                    ?: "Organize suas finanças semanais para manter o caixa saudável."

                _contasInsight.value = insight
                prefs.salvarInsightGerado("contas", periodo, hojeStr)

            } catch (e: Exception) {
                Log.e("GeminiAI", "Erro ao gerar insight Contas", e)
                _contasInsight.value =
                    "Controle rigoroso dos gastos diários garante tranquilidade no fim do mês."
            } finally {
                _contasIsLoading.value = false
            }
        }
    }

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