package com.raffastudioproducoes.minharota.ui.screens.dividas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.Divida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DividasViewModel : ViewModel() {

    private val _dividas = MutableStateFlow<List<Divida>>(emptyList())
    val dividas: StateFlow<List<Divida>> = _dividas.asStateFlow()

    fun carregarDividas(context: Context) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            _dividas.value = prefs.obterDividas()
        }
    }

    fun pagarParcela(context: Context, dividaId: String, valor: Double) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            val dividasAtuais = prefs.obterDividas().toMutableList()
            val index = dividasAtuais.indexOfFirst { it.id == dividaId }
            if (index != -1) {
                val d = dividasAtuais[index]
                val novoValorPago = (d.valorPago + valor).coerceAtMost(d.valorTotal)
                // Se pagou o valor de uma parcela (aproximadamente), incrementamos as parcelas pagas
                val valorParcela = if (d.totalParcelas > 0) d.valorTotal / d.totalParcelas else d.valorTotal
                val novasParcelasPagas = if (valor >= valorParcela * 0.9) {
                    (d.parcelasPagas + 1).coerceAtMost(d.totalParcelas)
                } else d.parcelasPagas

                val dividaAtualizada = d.copy(
                    valorPago = novoValorPago,
                    parcelasPagas = novasParcelasPagas
                )
                dividasAtuais[index] = dividaAtualizada
                prefs.salvarDividas(dividasAtuais)
                _dividas.value = dividasAtuais
            }
        }
    }

    fun quitarDivida(context: Context, dividaId: String) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            val dividasAtuais = prefs.obterDividas().toMutableList()
            val index = dividasAtuais.indexOfFirst { it.id == dividaId }
            if (index != -1) {
                val d = dividasAtuais[index]
                val dividaAtualizada = d.copy(
                    valorPago = d.valorTotal,
                    parcelasPagas = d.totalParcelas
                )
                dividasAtuais[index] = dividaAtualizada
                prefs.salvarDividas(dividasAtuais)
                _dividas.value = dividasAtuais
            }
        }
    }

    fun adicionarDivida(
        context: Context, 
        credor: String, 
        valorTotal: Double, 
        totalParcelas: Int = 1, 
        recorrencia: String = "Mês"
    ) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            val dividasAtuais = prefs.obterDividas().toMutableList()
            val novaDivida = Divida(
                id = UUID.randomUUID().toString(),
                credor = credor,
                valorTotal = valorTotal,
                valorPago = 0.0,
                totalParcelas = totalParcelas,
                parcelasPagas = 0,
                recorrencia = recorrencia
            )
            dividasAtuais.add(novaDivida)
            prefs.salvarDividas(dividasAtuais)
            _dividas.value = dividasAtuais
        }
    }

    fun editarDivida(context: Context, dividaEditada: Divida) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            val dividasAtuais = prefs.obterDividas().toMutableList()
            val index = dividasAtuais.indexOfFirst { it.id == dividaEditada.id }
            if (index != -1) {
                dividasAtuais[index] = dividaEditada
                prefs.salvarDividas(dividasAtuais)
                _dividas.value = dividasAtuais
            }
        }
    }

    fun excluirDivida(context: Context, dividaId: String) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            val dividasAtuais = prefs.obterDividas().filter { it.id != dividaId }
            prefs.salvarDividas(dividasAtuais)
            _dividas.value = dividasAtuais
        }
    }
}
