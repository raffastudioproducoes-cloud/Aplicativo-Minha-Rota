package com.raffastudioproducoes.minharota.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Safe placeholder while AI requests are moved behind an authenticated backend.
 * No model credential or direct model client is shipped in the APK.
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

    @Suppress("UNUSED_PARAMETER")
    fun gerarInsightHoje(
        context: Context,
        ganhoBruto: Double,
        horasTrabalhadas: String,
        ganhoLiquido: Double
    ) = disableAi()

    @Suppress("UNUSED_PARAMETER")
    fun gerarInsightGaragem(
        context: Context,
        kmTotal: Int,
        proximasManutencoes: String
    ) = disableAi()

    @Suppress("UNUSED_PARAMETER")
    fun gerarInsightContas(
        context: Context,
        dividasInfo: String
    ) = disableAi()

    private fun disableAi() {
        _hojeIsLoading.value = false
        _garagemIsLoading.value = false
        _contasIsLoading.value = false
        _errorMessage.value = null
    }

    fun limparInsights() {
        _hojeInsight.value = ""
        _garagemInsight.value = ""
        _contasInsight.value = ""
        disableAi()
    }
}
