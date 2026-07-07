package com.raffastudioproducoes.minharota.ui.screens.hoje

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.Corrida
import com.raffastudioproducoes.minharota.domain.model.Turno
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
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

    private val _faturamentoBrutoAcumulado = MutableStateFlow(0.0)
    val faturamentoBrutoAcumulado: StateFlow<Double> = _faturamentoBrutoAcumulado.asStateFlow()
    
    private val TETO_MEI_ANUAL = 81000.0
    private val ALERTA_MEI_THRESHOLD = 0.9

    private val _exibirAlertaMei = MutableStateFlow(false)
    val exibirAlertaMei: StateFlow<Boolean> = _exibirAlertaMei.asStateFlow()

    // --- Alerta de Renovação do Plano ---
    /** Número de dias restantes para o vencimento do plano (-1 = sem plano ativo). */
    private val _diasParaVencer = MutableStateFlow(-1L)
    val diasParaVencer: StateFlow<Long> = _diasParaVencer.asStateFlow()

    /** Nome do plano ativo (ex: "Premium", "Pro"). */
    private val _nomePlanoAtivo = MutableStateFlow("")
    val nomePlanoAtivo: StateFlow<String> = _nomePlanoAtivo.asStateFlow()

    /** Verdadeiro quando faltam 10 dias ou menos para o vencimento. */
    private val _exibirAlertaRenovacao = MutableStateFlow(false)
    val exibirAlertaRenovacao: StateFlow<Boolean> = _exibirAlertaRenovacao.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun carregarDadosMei(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _faturamentoBrutoAcumulado.value = prefs.obterFaturamentoBrutoAcumulado()
        // Carrega meta diária persistida (não é zerada ao salvar turno)
        val metaSalva = prefs.obterMetaDiaria()
        if (metaSalva > 0) _metaDiaria.value = metaSalva
        verificarAlertaMei()
        verificarVencimentoPlano(context)
    }

    /** Verifica a data de vencimento do plano e atualiza os estados de alerta. */
    fun verificarVencimentoPlano(context: Context) {
        val prefs = SharedPreferencesManager(context)
        val dataVencimentoStr = prefs.obterDataVencimento()
        val nomePlano = prefs.obterNomePlano()
        if (dataVencimentoStr.isBlank() || nomePlano == "Free") {
            _diasParaVencer.value = -1L
            _nomePlanoAtivo.value = ""
            _exibirAlertaRenovacao.value = false
            return
        }
        try {
            val dataVencimento = LocalDate.parse(dataVencimentoStr, DateTimeFormatter.ISO_LOCAL_DATE)
            val hoje = LocalDate.now()
            val dias = ChronoUnit.DAYS.between(hoje, dataVencimento)
            _diasParaVencer.value = dias
            _nomePlanoAtivo.value = nomePlano
            _exibirAlertaRenovacao.value = dias in 0..10
        } catch (e: Exception) {
            _diasParaVencer.value = -1L
            _exibirAlertaRenovacao.value = false
        }
    }

    private fun verificarAlertaMei() {
        _exibirAlertaMei.value = _faturamentoBrutoAcumulado.value >= (TETO_MEI_ANUAL * ALERTA_MEI_THRESHOLD)
    }

    fun updateGanhoBruto(valor: Double) {
        _ganhoBruto.value = valor
        calcularLiquido()
        calcularHorasTrabalhadas()
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

    fun updateMetaDiaria(valor: Double, context: Context? = null) {
        _metaDiaria.value = valor
        context?.let { SharedPreferencesManager(it).salvarMetaDiaria(valor) }
    }

    fun limparMetaDiaria(context: Context) {
        _metaDiaria.value = 0.0
        SharedPreferencesManager(context).salvarMetaDiaria(0.0)
    }

    fun toggleRidingMode() {
        _isRidingMode.value = !_isRidingMode.value
    }

    private fun calcularLiquido() {
        _ganhoLiquido.value = _ganhoBruto.value - _custoRua.value
    }

    fun registrarGanhoRapido(valor: Double) {
        if (valor <= 0) return
        val agora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val ganho = GanhoRapido(horario = agora, valor = valor)
        _ganhosRapidos.value = _ganhosRapidos.value + ganho

        val novaCorrida = Corrida(id = UUID.randomUUID().toString(), valor = valor, timestamp = System.currentTimeMillis())
        _corridasAtuais.value = _corridasAtuais.value + novaCorrida
        
        _ganhoBruto.value += valor
        calcularLiquido()
        calcularHorasTrabalhadas()
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

        // 1. Persistência Local
        val turnosAtuais = prefs.obterTurnos().toMutableList()
        turnosAtuais.add(novoTurno)
        prefs.salvarTurnos(turnosAtuais)

        val novoAcumulado = _faturamentoBrutoAcumulado.value + _ganhoBruto.value
        _faturamentoBrutoAcumulado.value = novoAcumulado
        prefs.salvarFaturamentoBrutoAcumulado(novoAcumulado)
        verificarAlertaMei()

        if (_ganhoLiquido.value > 0) {
            val caixinhas = prefs.obterCaixinhas().toMutableList()
            val novasCaixinhas = caixinhas.map { caixinha ->
                val valorAdicional = (_ganhoLiquido.value * caixinha.percentual) / 100.0
                caixinha.copy(saldoAtual = caixinha.saldoAtual + valorAdicional)
            }
            prefs.salvarCaixinhas(novasCaixinhas)
        }

        // 2. Sincronização Firebase (Firestore)
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                firestore.collection("usuarios")
                    .document(user.uid)
                    .collection("turnos")
                    .document(novoTurno.id)
                    .set(novoTurno)
                    .addOnSuccessListener { Log.d("Firebase", "Turno sincronizado!") }
                    .addOnFailureListener { e -> Log.e("Firebase", "Erro ao sincronizar", e) }
            }
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
        // Meta diária NÃO é zerada ao salvar turno — persiste até o usuário limpar manualmente
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
                    totalMinutes -= ChronoUnit.MINUTES.between(inicioPausa, fimPausa)
                }
            }
        } catch (e: Exception) { totalMinutes = 0L }
        if (totalMinutes < 0) totalMinutes = 0
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        _horasTrabalhadas.value = String.format("%02d:%02d", hours, minutes)
        _valorPorHora.value = if (totalMinutes > 0) _ganhoBruto.value / (totalMinutes / 60.0) else 0.0
    }
}
