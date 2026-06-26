package com.raffastudioproducoes.minharota.ui.screens.graficos

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val tendenciaGanhos by viewModel.tendenciaGanhos.collectAsState()

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

        // Heatmap Table
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            HeatmapTable(heatmapData)
        }

        Spacer(modifier = Modifier.height(12.dp))

        HeatmapLegend()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "📈 Tendência de Ganhos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Fluxo de lucro líquido dos últimos turnos",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de Ondas Real
        PremiumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (tendenciaGanhos.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Dados insuficientes para gerar gráfico (mín. 2 turnos)", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                }
            } else {
                WavePerformanceChart(tendenciaGanhos)
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

    val maxVal = data.flatMap { it.toList() }.maxOrNull() ?: 1.0
    val safeMaxVal = if (maxVal == 0.0) 1.0 else maxVal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Column {
            Row {
                Spacer(modifier = Modifier.width(35.dp))
                dias.forEach { dia ->
                    Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                        Text(text = dia, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            for (hora in 0..23) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${hora}h",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(35.dp),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    for (dia in 0..6) {
                        val valor = data[dia][hora]
                        val cellColor = when {
                            valor == 0.0 -> Color.White.copy(alpha = 0.03f)
                            valor < (safeMaxVal * 0.3) -> Color(0xFF5B21B6)
                            valor < (safeMaxVal * 0.6) -> Color(0xFF0284C7)
                            valor < (safeMaxVal * 0.9) -> Color(0xFF059669)
                            else -> Color(0xFFFBBF24)
                        }
                        val textColor = if (cellColor == Color(0xFFFBBF24)) Color.Black else Color.White

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(cellColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (valor > 0) {
                                Text(
                                    text = "R$ ${valor.toInt()}",
                                    fontSize = 8.sp,
                                    color = textColor,
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

@Composable
fun HeatmapLegend() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF5B21B6),
                        Color(0xFF0284C7),
                        Color(0xFF059669),
                        Color(0xFFFBBF24)
                    )
                )
            )
        ) {}
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Baixos", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
            Text("Médios", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
            Text("Ouro / Altos", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun WavePerformanceChart(data: List<Pair<String, Double>>) {
    val points = data.map { it.second.toFloat() }
    val labels = data.map { it.first }
    val maxVal = points.maxOrNull() ?: 1f
    val safeMaxVal = if (maxVal == 0f) 1f else maxVal
    
    Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val spacing = width / (points.size - 1)
            
            for (i in 1..4) {
                val y = height - (height / 5 * i)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val path = Path()
            val fillPath = Path()
            
            points.forEachIndexed { index, value ->
                val x = index * spacing
                val normalizedY = (value / safeMaxVal)
                val y = height - (normalizedY * height * 0.7f) - (height * 0.15f)
                
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (index - 1) * spacing
                    val prevNormalizedY = (points[index - 1] / safeMaxVal)
                    val prevY = height - (prevNormalizedY * height * 0.7f) - (height * 0.15f)
                    
                    path.cubicTo(
                        prevX + spacing / 2, prevY,
                        x - spacing / 2, y,
                        x, y
                    )
                    fillPath.cubicTo(
                        prevX + spacing / 2, prevY,
                        x - spacing / 2, y,
                        x, y
                    )
                }
                
                if (index == points.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )

            drawPath(
                path = path,
                color = Color(0xFF34D399),
                style = Stroke(width = 3.dp.toPx())
            )

            points.forEachIndexed { index, value ->
                val x = index * spacing
                val normalizedY = (value / safeMaxVal)
                val y = height - (normalizedY * height * 0.7f) - (height * 0.15f)
                drawCircle(
                    color = Color(0xFF34D399),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}
