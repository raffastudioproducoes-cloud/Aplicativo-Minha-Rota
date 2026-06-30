package com.raffastudioproducoes.minharota.ui.screens.garagem

import android.content.Context
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class Manutencao(
    val id: String,
    val nome: String,
    val intervaloKm: Int,
    val ultimoServicoKm: Int,
    val icone: String = "build",
    val concluida: Boolean = false,
    val dataConclusao: String = "",
    val kmConclusao: Int = 0
)

class GaragemViewModel : ViewModel() {

    private val _kmRodados = MutableStateFlow("")
    val kmRodados: StateFlow<String> = _kmRodados.asStateFlow()

    private val _litrosAbastecidos = MutableStateFlow("")
    val litrosAbastecidos: StateFlow<String> = _litrosAbastecidos.asStateFlow()

    private val _mediaResult = MutableStateFlow(0.0)
    val mediaResult: StateFlow<Double> = _mediaResult.asStateFlow()

    private val _kmAtual = MutableStateFlow(0)
    val kmAtual: StateFlow<Int> = _kmAtual.asStateFlow()

    private val _kmTotalAcumulado = MutableStateFlow(0)
    val kmTotalAcumulado: StateFlow<Int> = _kmTotalAcumulado.asStateFlow()

    private val _manutencoes = MutableStateFlow<List<Manutencao>>(emptyList())
    val manutencoes: StateFlow<List<Manutencao>> = _manutencoes.asStateFlow()

    fun carregarDados(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _kmAtual.value = prefs.obterKmAtual()
        _kmTotalAcumulado.value = prefs.obterKmTotal()
        _manutencoes.value = prefs.obterManutencoes()
    }

    /**
     * Atualiza o hodômetro e soma a diferença no KM Total Acumulado.
     */
    fun atualizarKmAtual(context: Context, novoKm: Int) {
        if (novoKm <= 0) return
        val prefs = SharedPreferencesManager(context)
        val kmAnterior = _kmAtual.value
        
        // Se o novo KM for maior que o anterior, somamos a diferença no total
        if (novoKm > kmAnterior && kmAnterior > 0) {
            val diferenca = novoKm - kmAnterior
            val novoTotal = _kmTotalAcumulado.value + diferenca
            _kmTotalAcumulado.value = novoTotal
            prefs.salvarKmTotal(novoTotal)
        }
        
        _kmAtual.value = novoKm
        prefs.salvarKmAtual(novoKm)
    }

    /**
     * Coleta automática de KM via RideAssistantService (Acessibilidade).
     * Soma o KM da corrida diretamente no KM Total e atualiza o KM Atual.
     */
    fun somarKmColetado(context: Context, kmColetado: Int) {
        if (kmColetado <= 0) return
        val prefs = SharedPreferencesManager(context)
        
        val novoTotal = _kmTotalAcumulado.value + kmColetado
        _kmTotalAcumulado.value = novoTotal
        prefs.salvarKmTotal(novoTotal)
        
        val novoKmAtual = _kmAtual.value + kmColetado
        _kmAtual.value = novoKmAtual
        prefs.salvarKmAtual(novoKmAtual)
    }

    fun adicionarManutencao(context: Context, nome: String, intervalo: Int, ultimo: Int, icone: String) {
        val prefs = SharedPreferencesManager(context)
        val nova = Manutencao(
            id = UUID.randomUUID().toString(), 
            nome = nome, 
            intervaloKm = intervalo, 
            ultimoServicoKm = ultimo, 
            icone = icone
        )
        val lista = _manutencoes.value + nova
        _manutencoes.value = lista
        prefs.salvarManutencoes(lista)
    }

    fun editarManutencao(context: Context, manutencaoEditada: Manutencao) {
        val prefs = SharedPreferencesManager(context)
        val lista = _manutencoes.value.map { 
            if (it.id == manutencaoEditada.id) manutencaoEditada else it 
        }
        _manutencoes.value = lista
        prefs.salvarManutencoes(lista)
    }

    fun concluirManutencao(context: Context, id: String) {
        val prefs = SharedPreferencesManager(context)
        val dataHoje = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val kmNoMomento = _kmAtual.value
        
        val lista = _manutencoes.value.map {
            if (it.id == id) {
                it.copy(
                    concluida = true, 
                    dataConclusao = dataHoje, 
                    kmConclusao = kmNoMomento,
                    ultimoServicoKm = kmNoMomento // Reseta o ciclo para o próximo intervalo
                )
            } else it
        }
        _manutencoes.value = lista
        prefs.salvarManutencoes(lista)
    }

    fun excluirManutencao(context: Context, id: String) {
        val prefs = SharedPreferencesManager(context)
        val lista = _manutencoes.value.filter { it.id != id }
        _manutencoes.value = lista
        prefs.salvarManutencoes(lista)
    }

    fun updateKm(value: String) {
        _kmRodados.value = value
        calcularMedia()
    }

    fun updateLitros(value: String) {
        _litrosAbastecidos.value = value
        calcularMedia()
    }

    private fun calcularMedia() {
        val k = _kmRodados.value.replace(",", ".").toDoubleOrNull() ?: 0.0
        val l = _litrosAbastecidos.value.replace(",", ".").toDoubleOrNull() ?: 0.0
        _mediaResult.value = if (l > 0) k / l else 0.0
    }
}
