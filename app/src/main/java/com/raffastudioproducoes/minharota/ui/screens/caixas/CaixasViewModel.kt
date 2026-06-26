package com.raffastudioproducoes.minharota.ui.screens.caixas

import android.content.Context
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.Caixinha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CaixasViewModel : ViewModel() {
    private val _caixinhas = MutableStateFlow<List<Caixinha>>(emptyList())
    val caixinhas: StateFlow<List<Caixinha>> = _caixinhas.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _showPaywallModal = MutableStateFlow(false)
    val showPaywallModal: StateFlow<Boolean> = _showPaywallModal.asStateFlow()

    private val _filtroPeriodo = MutableStateFlow("Hoje")
    val filtroPeriodo: StateFlow<String> = _filtroPeriodo.asStateFlow()

    private val _diasFolga = MutableStateFlow<Set<Int>>(emptySet())
    val diasFolga: StateFlow<Set<Int>> = _diasFolga.asStateFlow()

    private val _erroPercentual = MutableStateFlow<String?>(null)
    val erroPercentual: StateFlow<String?> = _erroPercentual.asStateFlow()

    fun carregarDados(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _isPro.value = prefs.obterIsPro()
        _caixinhas.value = prefs.obterCaixinhas()
        _diasFolga.value = prefs.obterDiasFolga()
        validarPercentuais()
    }

    private fun validarPercentuais() {
        val total = _caixinhas.value.sumOf { it.percentual }
        _erroPercentual.value = when {
            total > 100.0 -> "Atenção: A soma dos percentuais ultrapassa 100% (${total}%). O excedente não será distribuído corretamente."
            total < 100.0 -> "Atenção: A soma dos percentuais é inferior a 100% (${total}%). Parte do ganho ficará sem destino."
            else -> null
        }
    }

    fun setFiltroPeriodo(periodo: String) {
        _filtroPeriodo.value = periodo
    }

    fun toggleDiaFolga(context: Context, dia: Int) {
        val prefs = SharedPreferencesManager(context)
        val novosDias = _diasFolga.value.toMutableSet()
        if (novosDias.contains(dia)) novosDias.remove(dia) else novosDias.add(dia)
        _diasFolga.value = novosDias
        prefs.salvarDiasFolga(novosDias)
    }

    fun confirmarDeposito(context: Context, caixinhaId: String, valor: Double) {
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _caixinhas.value.toMutableList()
        val index = listaAtual.indexOfFirst { it.id == caixinhaId }
        
        if (index != -1) {
            val caixinha = listaAtual[index]
            listaAtual[index] = caixinha.copy(saldoAtual = caixinha.saldoAtual + valor)
            _caixinhas.value = listaAtual
            prefs.salvarCaixinhas(listaAtual)
        }
    }

    fun adicionarCaixinha(context: Context) {
        if (!_isPro.value && _caixinhas.value.size >= 3) {
            _showPaywallModal.value = true
            return
        }
        
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _caixinhas.value.toMutableList()
        val nova = Caixinha(
            id = UUID.randomUUID().toString(),
            nome = "Nova Caixinha",
            percentual = 0.0,
            emoji = "💰",
            cor = "#10B981"
        )
        listaAtual.add(nova)
        prefs.salvarCaixinhas(listaAtual)
        _caixinhas.value = listaAtual
        validarPercentuais()
    }

    fun atualizarCaixinha(context: Context, caixinha: Caixinha) {
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _caixinhas.value.map { if (it.id == caixinha.id) caixinha else it }
        _caixinhas.value = listaAtual
        prefs.salvarCaixinhas(listaAtual)
        validarPercentuais()
    }

    fun excluirCaixinha(context: Context, id: String) {
        val prefs = SharedPreferencesManager(context)
        val listaAtual = _caixinhas.value.filter { it.id != id }
        _caixinhas.value = listaAtual
        prefs.salvarCaixinhas(listaAtual)
        validarPercentuais()
    }

    fun dismissPaywallModal() {
        _showPaywallModal.value = false
    }

    fun upgradeToPro(context: Context) {
        val prefs = SharedPreferencesManager(context)
        prefs.salvarIsPro(true)
        _isPro.value = true
        _showPaywallModal.value = false
    }
}
