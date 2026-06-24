package com.raffastudioproducoes.minharota.ui.screens.hoje

import android.content.Context
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.Corrida
import com.raffastudioproducoes.minharota.domain.model.Turno
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class HojeViewModel : ViewModel() {
    private val _dataRegistro = MutableStateFlow(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    val dataRegistro: StateFlow<String> = _dataRegistro.asStateFlow()

    private val _ganhoBruto = MutableStateFlow(0.0)
    val ganhoBruto: StateFlow<Double> = _ganhoBruto.asStateFlow()

    private val _custoRua = MutableStateFlow(0.0)
    val custoRua: StateFlow<Double> = _custoRua.asStateFlow()

    data class CustoItem(val id: String, val descricao: String, val valor: Double)

    private val _listaCustos = MutableStateFlow<List<CustoItem>>(emptyList())
    val listaCustos: StateFlow<List<CustoItem>> = _listaCustos.asStateFlow()

    private val _metaDiaria = MutableStateFlow(0.0)
    val metaDiaria: StateFlow<Double> = _metaDiaria.asStateFlow()

    private val _ganhoLiquido = MutableStateFlow(0.0)
    val ganhoLiquido: StateFlow<Double> = _ganhoLiquido.asStateFlow()

    private val _valorPorHora = MutableStateFlow(0.0)
    val valorPorHora: StateFlow<Double> = _valorPorHora.asStateFlow()

    private val _horasTrabalhadas = MutableStateFlow("00:00")
    val horasTrabalhadas: StateFlow<String> = _horasTrabalhadas.asStateFlow()

    private val _isRidingMode = MutableStateFlow(false)
    val isRidingMode: StateFlow<Boolean> = _isRidingMode.asStateFlow()

    private val _horaInicio = MutableStateFlow("")
    val horaInicio: StateFlow<String> = _horaInicio.asStateFlow()

    private val _horaFim = MutableStateFlow("")
    val horaFim: StateFlow<String> = _horaFim.asStateFlow()

    private val _houvePausa = MutableStateFlow(false)
    val houvePausa: StateFlow<Boolean> = _houvePausa.asStateFlow()

    private val _horaInicioPausa = MutableStateFlow("")
    val horaInicioPausa: StateFlow<String> = _horaInicioPausa.asStateFlow()

    private val _horaFimPausa = MutableStateFlow("")
    val horaFimPausa: StateFlow<String> = _horaFimPausa.asStateFlow()

    private val _isFolga = MutableStateFlow(false)
    val isFolga: StateFlow<Boolean> = _isFolga.asStateFlow()

    data class GanhoRapido(val horario: String, val valor: Double)

    private val _ganhosRapidos = MutableStateFlow<List<GanhoRapido>>(emptyList())
    val ganhosRapidos: StateFlow<List<GanhoRapido>> = _ganhosRapidos.asStateFlow()

    private val _corridasAtuais = MutableStateFlow<List<Corrida>>(emptyList())

    // Lógica MEI
    private val _faturamentoBrutoAcumulado = MutableStateFlow(0.0)
    val faturamentoBrutoAcumulado: StateFlow<Double> = _faturamentoBrutoAcumulado.asStateFlow()
    
    private val TETO_MEI_ANUAL = 81000.0
    private val ALERTA_MEI_THRESHOLD = 0.9 // 90% do teto

    private val _exibirAlertaMei = MutableStateFlow(false)
    val exibirAlertaMei: StateFlow<Boolean> = _exibirAlertaMei.asStateFlow()

    fun carregarDadosMei(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _faturamentoBrutoAcumulado.value = prefs.obterFaturamentoBrutoAcumulado()
        verificarAlertaMei()
    }

    private fun verificarAlertaMei() {
        _exibirAlertaMei.value = _faturamentoBrutoAcumulado.value >= (TETO_MEI_ANUAL * ALERTA_MEI_THRESHOLD)
    }

    fun updateGanhoBruto(valor: Double) {
        _ganhoBruto.value = valor
        calcularLiquido()
        calcularHorasTrabalhadas()
    }

    fun updateCustoRua(valor: Double) {
        _custoRua.value = valor
        calcularLiquido()
    }

    fun adicionarCusto(descricao: String, valor: Double) {
        if (valor <= 0 || descricao.isBlank()) return
        val novoCusto = CustoItem(UUID.randomUUID().toString(), descricao, valor)
        _listaCustos.value = _listaCustos.value + novoCusto
        _custoRua.value = _listaCustos.value.sumOf { it.valor }
        calcularLiquido()
    }

    fun removerCusto(id: String) {
        _listaCustos.value = _listaCustos.value.filter { it.id != id }
        _custoRua.value = _listaCustos.value.sumOf { it.valor }
        calcularLiquido()
    }

    fun updateMetaDiaria(valor: Double) {
        _metaDiaria.value = valor
    }

    fun toggleRidingMode() {
        _isRidingMode.value = !_isRidingMode.value
    }

    private fun calcularLiquido() {
        _ganhoLiquido.value = _ganhoBruto.value - _custoRua.value
    }

    private fun calcularGanhoLiquido() {
        calcularLiquido()
    }

    fun adicionarGanhoRapido(valor: Double) {
        if (valor <= 0) return

        // Registrar na lista visual da tela Hoje
        val agora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val ganho = GanhoRapido(horario = agora, valor = valor)
        _ganhosRapidos.value = _ganhosRapidos.value + ganho

        // Registrar na lista de persistência do Turno (Corrida)
        val novaCorrida = Corrida(
            id = UUID.randomUUID().toString(),
            valor = valor,
            timestamp = System.currentTimeMillis()
        )
        _corridasAtuais.value = _corridasAtuais.value + novaCorrida
        
        // Atualizar valores totais acumulados
        _ganhoBruto.value += valor
        calcularLiquido()
        calcularHorasTrabalhadas()
    }

    // Função unificada para registrar ganho vindo do modal ou riding mode
    fun registrarGanhoRapido(valor: Double) {
        adicionarGanhoRapido(valor)
    }

    fun salvarTurno(context: Context, onSuccess: () -> Unit) {
        val prefs = SharedPreferencesManager(context)
        
        val novoTurno = Turno(
            id = UUID.randomUUID().toString(),
            data = _dataRegistro.value,
            horaInicio = _horaInicio.value,
            horaFim = _horaFim.value,
            houvePausa = _houvePausa.value,
            horaInicioPausa = _horaInicioPausa.value,
            horaFimPausa = _horaFimPausa.value,
            ganhoBruto = _ganhoBruto.value,
            custoRua = _custoRua.value,
            ganhoLiquido = _ganhoLiquido.value,
            corridas = _corridasAtuais.value
        )

        // 1. Persistir o turno
        val turnosAtuais = prefs.obterTurnos().toMutableList()
        turnosAtuais.add(novoTurno)
        prefs.salvarTurnos(turnosAtuais)

        // 2. Lógica MEI: Somar ganho bruto ao acumulado
        val novoAcumulado = _faturamentoBrutoAcumulado.value + _ganhoBruto.value
        _faturamentoBrutoAcumulado.value = novoAcumulado
        prefs.salvarFaturamentoBrutoAcumulado(novoAcumulado)
        verificarAlertaMei()

        // 3. Distribuir para as caixinhas
        if (_ganhoLiquido.value > 0) {
            val caixinhas = prefs.obterCaixinhas().toMutableList()
            val novasCaixinhas = caixinhas.map { caixinha ->
                val valorAdicional = (_ganhoLiquido.value * caixinha.percentual) / 100.0
                caixinha.copy(saldoAtual = caixinha.saldoAtual + valorAdicional)
            }
            prefs.salvarCaixinhas(novasCaixinhas)
        }
        
        limparCampos()
        onSuccess()
    }

    private fun limparCampos() {
        _dataRegistro.value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _ganhoBruto.value = 0.0
        _custoRua.value = 0.0
        _ganhoLiquido.value = 0.0
        _horaInicio.value = ""
        _horaFim.value = ""
        _houvePausa.value = false
        _horaInicioPausa.value = ""
        _horaFimPausa.value = ""
        _corridasAtuais.value = emptyList()
        _ganhosRapidos.value = emptyList()
        _listaCustos.value = emptyList()
        _metaDiaria.value = 0.0
    }

    fun updateHoraInicio(hora: String) {
        _horaInicio.value = hora
        calcularHorasTrabalhadas()
    }

    fun updateHoraFim(hora: String) {
        _horaFim.value = hora
        calcularHorasTrabalhadas()
    }

    fun updateHouvePausa(houve: Boolean) {
        _houvePausa.value = houve
        if (!houve) {
            _horaInicioPausa.value = ""
            _horaFimPausa.value = ""
        }
        calcularHorasTrabalhadas()
    }

    fun updateHoraInicioPausa(hora: String) {
        _horaInicioPausa.value = hora
        calcularHorasTrabalhadas()
    }

    fun updateHoraFimPausa(hora: String) {
        _horaFimPausa.value = hora
        calcularHorasTrabalhadas()
    }

    fun toggleFolga() {
        _isFolga.value = !_isFolga.value
        if (_isFolga.value) {
            _metaDiaria.value = 0.0
        }
    }

    fun alterarDataRegistro(novaData: String) {
        _dataRegistro.value = novaData
    }

    private fun calcularHorasTrabalhadas() {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        var totalMinutes = 0L

        try {
            if (_horaInicio.value.isNotBlank() && _horaFim.value.isNotBlank()) {
                val inicio = LocalTime.parse(_horaInicio.value, formatter)
                val fim = LocalTime.parse(_horaFim.value, formatter)
                totalMinutes = ChronoUnit.MINUTES.between(inicio, fim)

                if (_houvePausa.value && _horaInicioPausa.value.isNotBlank() && _horaFimPausa.value.isNotBlank()) {
                    val inicioPausa = LocalTime.parse(_horaInicioPausa.value, formatter)
                    val fimPausa = LocalTime.parse(_horaFimPausa.value, formatter)
                    val minutosPausa = ChronoUnit.MINUTES.between(inicioPausa, fimPausa)
                    totalMinutes -= minutosPausa
                }
            }
        } catch (e: Exception) {
            totalMinutes = 0L
        }

        if (totalMinutes < 0) totalMinutes = 0

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        _horasTrabalhadas.value = String.format("%02d:%02d", hours, minutes)
        
        _valorPorHora.value = if (totalMinutes > 0) _ganhoBruto.value / (totalMinutes / 60.0) else 0.0
    }
}
