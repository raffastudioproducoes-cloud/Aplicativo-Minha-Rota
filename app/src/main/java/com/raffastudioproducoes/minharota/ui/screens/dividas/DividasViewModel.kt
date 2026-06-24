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
                val dividaAtualizada = dividasAtuais[index].copy(valorPago = dividasAtuais[index].valorPago + valor)
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
                val dividaAtualizada = dividasAtuais[index].copy(valorPago = dividasAtuais[index].valorTotal)
                dividasAtuais[index] = dividaAtualizada
                prefs.salvarDividas(dividasAtuais)
                _dividas.value = dividasAtuais
            }
        }
    }

    fun adicionarDivida(context: Context, credor: String, valorTotal: Double) {
        viewModelScope.launch {
            val prefs = SharedPreferencesManager(context)
            val dividasAtuais = prefs.obterDividas().toMutableList()
            val novaDivida = Divida(
                id = java.util.UUID.randomUUID().toString(),
                credor = credor,
                valorTotal = valorTotal,
                valorPago = 0.0
            )
            dividasAtuais.add(novaDivida)
            prefs.salvarDividas(dividasAtuais)
            _dividas.value = dividasAtuais
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
