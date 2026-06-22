package com.raffastudioproducoes.minharota.ui.screens.contas

import android.content.Context
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.ContaFixa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ContasViewModel : ViewModel() {
    private val _contas = MutableStateFlow<List<ContaFixa>>(emptyList())
    val contas: StateFlow<List<ContaFixa>> = _contas.asStateFlow()

    private val _metaDiariaAutomatica = MutableStateFlow(0.0)
    val metaDiariaAutomatica: StateFlow<Double> = _metaDiariaAutomatica.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _faturamentoAnual = MutableStateFlow(0.0)
    val faturamentoAnual: StateFlow<Double> = _faturamentoAnual.asStateFlow()

    private val _limiteMei = MutableStateFlow(81000.0)
    val limiteMei: StateFlow<Double> = _limiteMei.asStateFlow()

    fun carregarContas(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _isPro.value = prefs.obterIsPro()
        
        val turnos = prefs.obterTurnos()
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR).toString()
        _faturamentoAnual.value = turnos
            .filter { it.data.endsWith(anoAtual) }
            .sumOf { it.ganhoLiquido }
            
        val lista = prefs.obterContas()
        _contas.value = lista
        calcularMeta()
    }

    private fun calcularMeta() {
        val totalPendente = _contas.value.filter { !it.paga }.sumOf { it.valor }
        // Simulação: divide o total pendente por 30 dias para meta diária
        _metaDiariaAutomatica.value = totalPendente / 30.0
    }

    fun adicionarConta(context: Context, nome: String, valor: Double, vencimento: String) {
        val prefs = SharedPreferencesManager(context)
        val novaConta = ContaFixa(UUID.randomUUID().toString(), nome, valor, vencimento)
        val listaAtual = _contas.value.toMutableList()
        listaAtual.add(novaConta)
        _contas.value = listaAtual
        prefs.salvarContas(listaAtual)
        calcularMeta()
    }

    fun atualizarConta(context: Context, id: String, nome: String, valor: Double, vencimento: String) {
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _contas.value.toMutableList()
        val index = listaAtual.indexOfFirst { it.id == id }
        if (index != -1) {
            listaAtual[index] = listaAtual[index].copy(nome = nome, valor = valor, dataVencimento = vencimento)
            _contas.value = listaAtual
            prefs.salvarContas(listaAtual)
            calcularMeta()
        }
    }

    fun excluirConta(context: Context, id: String) {
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _contas.value.filter { it.id != id }
        _contas.value = listaAtual
        prefs.salvarContas(listaAtual)
        calcularMeta()
    }

    fun pagarConta(context: Context, contaId: String) {
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _contas.value.toMutableList()
        val index = listaAtual.indexOfFirst { it.id == contaId }
        if (index != -1) {
            listaAtual[index] = listaAtual[index].copy(paga = !listaAtual[index].paga)
            _contas.value = listaAtual
            prefs.salvarContas(listaAtual)
            calcularMeta()
        }
    }

    fun agendarAlertasMei(context: Context) {
        if (!_isPro.value) return
        // Lógica de agendamento de notificações locais seria implementada aqui
        // usando WorkManager ou AlarmManager conforme a necessidade.
    }
}
