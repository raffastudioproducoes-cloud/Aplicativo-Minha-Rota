package com.raffastudioproducoes.minharota.ui.screens.graficos

import android.content.Context
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.Turno
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class GraficosViewModel : ViewModel() {
    // Mapa de Calor (Horários de Ouro)
    private val _heatmapData = MutableStateFlow(Array(7) { DoubleArray(24) { 0.0 } })
    val heatmapData: StateFlow<Array<DoubleArray>> = _heatmapData.asStateFlow()

    private val _melhorDia = MutableStateFlow("---")
    val melhorDia: StateFlow<String> = _melhorDia.asStateFlow()

    private val _melhorHora = MutableStateFlow("--h")
    val melhorHora: StateFlow<String> = _melhorHora.asStateFlow()

    // Ganhos Brutos Semanais (Gráfico de Barras)
    private val _ganhosSemanais = MutableStateFlow<List<Double>>(List(7) { 0.0 })
    val ganhosSemanais: StateFlow<List<Double>> = _ganhosSemanais.asStateFlow()

    private val _semanaSelecionadaOffset = MutableStateFlow(0) // 0 = Esta Semana, 1 = Semana Passada, etc.
    val semanaSelecionadaOffset: StateFlow<Int> = _semanaSelecionadaOffset.asStateFlow()

    private val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    fun carregarDados(context: Context) {
        val prefs = SharedPreferencesManager(context)
        // Unificação de Fontes: Turnos + Histórico de Lançamentos Rápidos (já inclusos no Turno.corridas se salvos)
        val turnos = prefs.obterTurnos()
            .distinctBy { it.id } // Eliminar duplicatas por ID
            .sortedBy { 
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.data)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        
        processarDados(turnos)
    }

    fun setSemanaOffset(offset: Int, context: Context) {
        _semanaSelecionadaOffset.value = offset
        carregarDados(context)
    }

    private fun processarDados(turnos: List<Turno>) {
        val novaMatriz = Array(7) { DoubleArray(24) { 0.0 } }
        val novosGanhosSemanais = MutableList(7) { 0.0 }
        
        val calRef = Calendar.getInstance()
        calRef.add(Calendar.WEEK_OF_YEAR, -_semanaSelecionadaOffset.value)
        
        // Definir início e fim da semana selecionada
        calRef.set(Calendar.DAY_OF_WEEK, calRef.firstDayOfWeek)
        calRef.set(Calendar.HOUR_OF_DAY, 0)
        calRef.set(Calendar.MINUTE, 0)
        calRef.set(Calendar.SECOND, 0)
        calRef.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calRef.timeInMillis
        
        calRef.add(Calendar.DAY_OF_WEEK, 6)
        calRef.set(Calendar.HOUR_OF_DAY, 23)
        calRef.set(Calendar.MINUTE, 59)
        calRef.set(Calendar.SECOND, 59)
        val endOfWeek = calRef.timeInMillis

        var maxGanhoHora = 0.0
        var melhorHoraValor = -1
        val ganhosPorDiaSemana = DoubleArray(7) { 0.0 }

        turnos.forEach { turno ->
            val date = try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(turno.data)
            } catch (e: Exception) {
                null
            }
            
            if (date != null) {
                val turnoTime = date.time
                
                // Filtrar rigorosamente pela semana selecionada usando timestamps
                if (turnoTime in startOfWeek..endOfWeek) {
                    val turnoCal = Calendar.getInstance()
                    turnoCal.time = date
                    val diaIndex = turnoCal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Dom
                    
                    // 1. Processar para o Gráfico de Barras (Ganho Bruto por Dia)
                    novosGanhosSemanais[diaIndex] += turno.ganhoBruto
                    ganhosPorDiaSemana[diaIndex] += turno.ganhoBruto

                    // 2. Processar para o Heatmap (Unificação de Fontes: Todas as corridas do turno)
                    turno.corridas.forEach { corrida ->
                        val corridaCal = Calendar.getInstance()
                        corridaCal.timeInMillis = corrida.timestamp
                        val hora = corridaCal.get(Calendar.HOUR_OF_DAY)
                        
                        novaMatriz[diaIndex][hora] += corrida.valor
                        
                        if (novaMatriz[diaIndex][hora] > maxGanhoHora) {
                            maxGanhoHora = novaMatriz[diaIndex][hora]
                            melhorHoraValor = hora
                        }
                    }
                }
            }
        }

        // Encontrar melhor dia da semana selecionada
        var maxGanhoDiaSemana = 0.0
        var indexMelhorDia = -1
        for (i in 0..6) {
            if (ganhosPorDiaSemana[i] > maxGanhoDiaSemana) {
                maxGanhoDiaSemana = ganhosPorDiaSemana[i]
                indexMelhorDia = i
            }
        }

        _heatmapData.value = novaMatriz
        _ganhosSemanais.value = novosGanhosSemanais
        _melhorDia.value = if (indexMelhorDia != -1) diasSemana[indexMelhorDia] else "---"
        _melhorHora.value = if (melhorHoraValor != -1) "${melhorHoraValor}h" else "--h"
    }
}
