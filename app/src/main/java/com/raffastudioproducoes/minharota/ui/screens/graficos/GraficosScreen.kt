package com.raffastudioproducoes.minharota.ui.screens.graficos

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun GraficosScreen(viewModel: GraficosViewModel = viewModel()) {
    val context = LocalContext.current
    val heatmapData by viewModel.heatmapData.collectAsState()
    val melhorDia by viewModel.melhorDia.collectAsState()
    val melhorHora by viewModel.melhorHora.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarDados(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Análise de Performance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cards de Destaque Glassmorphic
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HighlightCard(
                title = "Melhor Dia",
                value = melhorDia,
                modifier = Modifier.weight(1f),
                color = VerdeNeon
            )
            HighlightCard(
                title = "Melhor Hora",
                value = melhorHora,
                modifier = Modifier.weight(1f),
                color = Color(0xFF34D399) // Verde Menta
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "⭐ Horários de Ouro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Mapa de calor baseado nos seus ganhos reais",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Table envolto em Glass
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            HeatmapTable(heatmapData)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Placeholder para o outro gráfico em Glass
        PremiumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Gráfico Ganhos vs Despesas\n(Próxima Fase)", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HighlightCard(title: String, value: String, modifier: Modifier, color: Color) {
    PremiumGlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun HeatmapTable(data: Array<DoubleArray>) {
    val dias = listOf("D", "S", "T", "Q", "Q", "S", "S")
    val scrollState = rememberScrollState()

    // Encontrar o valor máximo para normalizar as cores
    val maxVal = data.flatMap { it.toList() }.maxOrNull() ?: 1.0
    val safeMaxVal = if (maxVal == 0.0) 1.0 else maxVal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Column {
            // Header Dias
            Row {
                Spacer(modifier = Modifier.width(30.dp))
                dias.forEach { dia ->
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Text(text = dia, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // Horas e Células (00h às 23h)
            for (hora in 0..23) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${hora}h",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(30.dp),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    for (dia in 0..6) {
                        val valor = data[dia][hora]
                        val intensidade = (valor / safeMaxVal).toFloat()
                        
                        // Lógica de cores Fintech Premium
                        val cellColor = if (valor > 0) {
                            VerdeNeon.copy(alpha = (0.2f + (intensidade * 0.8f)).coerceAtMost(1f))
                        } else {
                            Color.White.copy(alpha = 0.05f)
                        }

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(cellColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (valor > 0) {
                                Text(
                                    text = if (valor >= 100) "99+" else valor.toInt().toString(),
                                    fontSize = 9.sp,
                                    color = if (intensidade > 0.5f) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
