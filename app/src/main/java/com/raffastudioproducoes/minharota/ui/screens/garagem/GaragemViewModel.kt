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

@Serializable
data class Manutencao(
    val id: String,
    val nome: String,
    val intervaloKm: Int,
    val ultimoServicoKm: Int,
    val icone: String = "build"
)

class GaragemViewModel : ViewModel() {

    private val _kmRodados = MutableStateFlow("")
    val kmRodados: StateFlow<String> = _kmRodados.asStateFlow()

    private val _litrosAbastecidos = MutableStateFlow("")
    val litrosAbastecidos: StateFlow<String> = _litrosAbastecidos.asStateFlow()

    val mediaKmL: StateFlow<Double> = combine(_kmRodados, _litrosAbastecidos) { km, litros ->
        val k = km.toDoubleOrNull() ?: 0.0
        val l = litros.toDoubleOrNull() ?: 0.0
        if (l > 0) k / l else 0.0
    }.let { flow ->
        val state = MutableStateFlow(0.0)
        // No ViewModel, não podemos coletar diretamente sem um scope, mas o combine resolve o estado reativo
        // Em um app real, usaríamos stateIn(viewModelScope)
        state
    }
    
    // Simplificando para o StateFlow reativo funcionar corretamente no Compose
    private val _mediaResult = MutableStateFlow(0.0)
    val mediaResult: StateFlow<Double> = _mediaResult.asStateFlow()

    private val _kmAtual = MutableStateFlow(0)
    val kmAtual: StateFlow<Int> = _kmAtual.asStateFlow()

    private val _manutencoes = MutableStateFlow<List<Manutencao>>(emptyList())
    val manutencoes: StateFlow<List<Manutencao>> = _manutencoes.asStateFlow()

    fun carregarDados(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _kmAtual.value = prefs.obterKmAtual()
        _manutencoes.value = prefs.obterManutencoes()
    }

    fun atualizarKmAtual(context: Context, novoKm: Int) {
        val prefs = SharedPreferencesManager(context)
        _kmAtual.value = novoKm
        prefs.salvarKmAtual(novoKm)
    }

    fun adicionarManutencao(context: Context, nome: String, intervalo: Int, ultimo: Int, icone: String) {
        val prefs = SharedPreferencesManager(context)
        val nova = Manutencao(UUID.randomUUID().toString(), nome, intervalo, ultimo, icone)
        val lista = _manutencoes.value + nova
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
