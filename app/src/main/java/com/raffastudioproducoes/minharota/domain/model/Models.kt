package com.raffastudioproducoes.minharota.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Corrida(val id: String, val valor: Double, val timestamp: Long)

@Serializable
data class Turno(
    val id: String,
    val data: String,
    val horaInicio: String,
    val horaFim: String,
    val houvePausa: Boolean = false,
    val horaInicioPausa: String = "",
    val horaFimPausa: String = "",
    val ganhoBruto: Double,
    val custoRua: Double,
    val ganhoLiquido: Double,
    val corridas: List<Corrida> = emptyList()
)

@Serializable
data class Caixinha(
    val id: String,
    val nome: String,
    val subtitulo: String = "",
    val emoji: String = "💰",
    val cor: String = "#2ECC71",
    val percentual: Double,
    val metaValor: Double = 0.0,
    val saldoAtual: Double = 0.0,
    val pausada: Boolean = false,
    val periodo: String = "Dia" // Dia, Semana, Mês, Ano
)

@Serializable
data class Movimentacao(
    val id: String,
    val tipo: String,
    val descricao: String,
    val valor: Double,
    val data: String
)

@Serializable
data class ContaFixa(
    val id: String,
    val nome: String,
    val valor: Double,
    val dataVencimento: String,
    val paga: Boolean = false
)

@Serializable
data class Divida(
    val id: String,
    val credor: String,
    val valorTotal: Double,
    val valorPago: Double = 0.0
)

@Serializable
data class ContaDiaria(
    val id: String,
    val descricao: String,
    val valor: Double,
    val frequencia: String // "Diario", "Semanal", "Quinzenal", "Mensal"
)

@Serializable
data class Veiculo(
    val id: String = "veiculo_unico", // Simplificando para um único veículo por enquanto
    val quilometragemAtual: Double,
    val manutencoes: List<Manutencao> = emptyList()
) {
    @Serializable
    data class Manutencao(
        val tipo: String,
        val kmUltimoServico: Double,
        val intervaloKm: Double = 0.0, // Intervalo padrão para o próximo serviço
        val proximoServicoKm: Double // KM total para o próximo serviço (kmUltimoServico + intervaloKm)
    )
}
