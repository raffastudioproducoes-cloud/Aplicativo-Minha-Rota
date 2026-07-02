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

    // Tendência de Ganhos (Gráfico de Linha — últimos 30 dias, agregado por data única)
    data class PontoTendencia(val label: String, val valor: Double)
    private val _tendenciaGanhos = MutableStateFlow<List<PontoTendencia>>(emptyList())
    val tendenciaGanhos: StateFlow<List<PontoTendencia>> = _tendenciaGanhos.asStateFlow()

    // Ganhos vs Despesas v2.0
    private val _totalGanhosSemana = MutableStateFlow(0.0)
    val totalGanhosSemana: StateFlow<Double> = _totalGanhosSemana.asStateFlow()

    private val _totalDespesasSemana = MutableStateFlow(0.0)
    val totalDespesasSemana: StateFlow<Double> = _totalDespesasSemana.asStateFlow()

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

                    // 2. Processar para o Heatmap (Unificação de Fontes)
                    // Se o turno tem corridas individuais (FAB), usamos os horários delas
                    if (turno.corridas.isNotEmpty()) {
                        turno.corridas.forEach { corrida ->
                            val corridaCal = Calendar.getInstance()
                            corridaCal.timeInMillis = corrida.timestamp
                            val hora = corridaCal.get(Calendar.HOUR_OF_DAY)
                            
                            novaMatriz[diaIndex][hora] += corrida.valor
                        }
                    } else {
                        // Se o turno foi fechado manualmente sem lançamentos individuais (OCR/Manual),
                        // distribuímos o ganho bruto no horário de início do turno
                        val horaInicio = try {
                            turno.horaInicio.split(":")[0].toInt()
                        } catch (e: Exception) {
                            12 // Fallback para meio-dia
                        }
                        novaMatriz[diaIndex][horaInicio] += turno.ganhoBruto
                    }
                }
            }
        }

        // Atualizar Melhor Hora após processar todos os ganhos unificados
        for (d in 0..6) {
            for (h in 0..23) {
                if (novaMatriz[d][h] > maxGanhoHora) {
                    maxGanhoHora = novaMatriz[d][h]
                    melhorHoraValor = h
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

        // Calcular Totais v2.0
        _totalGanhosSemana.value = novosGanhosSemanais.sum()
        // Para despesas, somamos os custos fixos + variáveis dos turnos da semana
        var despesasAcumuladas = 0.0
        turnos.forEach { turno ->
            val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(turno.data) } catch (e: Exception) { null }
            if (date != null && date.time in startOfWeek..endOfWeek) {
                despesasAcumuladas += turno.custoTotal
            }
        }
        _totalDespesasSemana.value = despesasAcumuladas

        // Calcular tendência: todos os turnos, agregados por data única, ordenados cronologicamente
        calcularTendencia(turnos)
    }

    private fun calcularTendencia(turnos: List<Turno>) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        // Agregar ganho bruto por data única (evita duplicação do mesmo dia)
        val agrupado = LinkedHashMap<String, Double>()
        turnos.forEach { turno ->
            val dataKey = turno.data
            agrupado[dataKey] = (agrupado[dataKey] ?: 0.0) + turno.ganhoBruto
        }
        // Ordenar cronologicamente e pegar os últimos 30 pontos
        val pontos = agrupado.entries
            .sortedBy { entry ->
                try { sdf.parse(entry.key)?.time ?: 0L } catch (e: Exception) { 0L }
            }
            .takeLast(30)
            .map { entry ->
                val labelCurto = try {
                    val d = sdf.parse(entry.key)
                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(d!!)
                } catch (e: Exception) { entry.key }
                PontoTendencia(label = labelCurto, valor = entry.value)
            }
        _tendenciaGanhos.value = pontos
    }
}
