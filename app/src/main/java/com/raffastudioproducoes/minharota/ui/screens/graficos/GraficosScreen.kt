package com.raffastudioproducoes.minharota.ui.screens.graficos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun GraficosScreen(viewModel: GraficosViewModel = viewModel()) {
    val context = LocalContext.current
    val heatmapData by viewModel.heatmapData.collectAsState()
    val melhorDia by viewModel.melhorDia.collectAsState()
    val melhorHora by viewModel.melhorHora.collectAsState()
    val ganhosSemanais by viewModel.ganhosSemanais.collectAsState()
    val semanaOffset by viewModel.semanaSelecionadaOffset.collectAsState()
    val tendenciaGanhos by viewModel.tendenciaGanhos.collectAsState()
    val totalGanhos by viewModel.totalGanhosSemana.collectAsState()
    val totalDespesas by viewModel.totalDespesasSemana.collectAsState()

    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val subTextColor = textColor.copy(alpha = 0.5f)

    LaunchedEffect(Unit) {
        viewModel.carregarDados(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Performance Operacional",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seletor de Semana Mestre
        WeekSelector(
            offset = semanaOffset,
            onOffsetChange = { viewModel.setSemanaOffset(it, context) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NOVO: CARD DUAL DINÂMICO (Ganhos vs Despesas) v2.0
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GANHOS", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    }
                    Text(
                        text = "R$ ${String.format("%.2f", totalGanhos)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = VerdeNeon
                    )
                }
                
                // Divisor Vertical v2.0
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(textColor.copy(alpha = 0.1f)))
                
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.TrendingDown, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DESPESAS", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    }
                    Text(
                        text = "R$ ${String.format("%.2f", totalDespesas)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF87171)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cards de Destaque
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
                color = Color(0xFF34D399)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CARD: GANHOS BRUTOS SEMANAIS
        Text(
            text = "📊 Ganhos Brutos da Semana",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        PremiumGlassCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            WeeklyBarChart(ganhosSemanais)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "⭐ Horários de Ouro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Faturamento real por hora e dia",
            style = MaterialTheme.typography.labelSmall,
            color = subTextColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Table
        PremiumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HeatmapTable(heatmapData)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HeatmapLegend()

        Spacer(modifier = Modifier.height(32.dp))

        // TENDÊNCIA DE GANHOS
        if (tendenciaGanhos.isNotEmpty()) {
            Text(
                text = "📈 Tendência de Ganhos (30 dias)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "Ganho bruto diário agregado, em ordem cronológica",
                style = MaterialTheme.typography.labelSmall,
                color = subTextColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            PremiumGlassCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                TrendLineChart(tendenciaGanhos)
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun TrendLineChart(pontos: List<GraficosViewModel.PontoTendencia>) {
    if (pontos.isEmpty()) return
    
    val isDark = isSystemInDarkTheme()
    val exibicaoPontos = if (pontos.size == 1) listOf(pontos[0], pontos[0]) else pontos
    val maxVal = exibicaoPontos.maxOf { it.valor }.coerceAtLeast(1.0)

    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
        val w = size.width
        val h = size.height
        val stepX = w / (exibicaoPontos.size - 1).coerceAtLeast(1).toFloat()

        val pontosPx = exibicaoPontos.mapIndexed { i, p ->
            Offset(
                x = i * stepX,
                y = h - (p.valor / maxVal * h * 0.75f).toFloat().coerceIn(0f, h)
            )
        }

        val fillPath = androidx.compose.ui.graphics.Path()
        fillPath.moveTo(0f, h)
        pontosPx.forEach { fillPath.lineTo(it.x, it.y) }
        fillPath.lineTo(w, h)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(VerdeNeon.copy(alpha = 0.2f), Color.Transparent)
            )
        )

        for (i in 0 until pontosPx.size - 1) {
            drawLine(
                color = VerdeNeon,
                start = pontosPx[i],
                end = pontosPx[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }

        pontosPx.forEachIndexed { i, pt ->
            if (pontos.size > 1 || i == 0) {
                drawCircle(
                    color = VerdeNeon,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = if (isDark) Color(0xFF0C0C0E) else Color.White,
                    radius = 2.5.dp.toPx(),
                    center = pt
                )

                val label = exibicaoPontos[i].label
                if (i == 0 || i == exibicaoPontos.size - 1 || i % 4 == 0) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            alpha = 100
                            textSize = 9.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(label, pt.x, h + 18.dp.toPx(), paint)
                    }
                }
            }
        }
    }
}

@Composable
fun WeekSelector(offset: Int, onOffsetChange: (Int) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val label = when (offset) {
        0 -> "Esta Semana"
        1 -> "Semana Passada"
        else -> "$offset Semanas Atrás"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onOffsetChange(offset + 1) }) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = if (isDark) Color.White else Color.Black)
        }
        
        Text(
            text = label,
            color = if (isDark) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        IconButton(
            onClick = { if (offset > 0) onOffsetChange(offset - 1) },
            enabled = offset > 0
        ) {
            Icon(
                Icons.Rounded.ChevronRight, 
                contentDescription = null, 
                tint = if (offset > 0) (if (isDark) Color.White else Color.Black) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f))
            )
        }
    }
}

@Composable
fun WeeklyBarChart(ganhos: List<Double>) {
    val labels = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
    val maxGanho = ganhos.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val isDark = isSystemInDarkTheme()

    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 24.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = (width / 7) * 0.6f
        val spacing = (width / 7)
        
        ganhos.forEachIndexed { index, valor ->
            val barHeight = (valor / maxGanho).toFloat() * height * 0.7f
            val x = (index * spacing) + (spacing - barWidth) / 2
            val y = height - barHeight - 20.dp.toPx()

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.3f))
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.apply {
                val text = "R$ ${valor.toInt()}"
                val paint = android.graphics.Paint().apply {
                    color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                drawText(text, x + barWidth / 2, y - 8.dp.toPx(), paint)
                
                paint.apply {
                    alpha = 128
                    textSize = 9.dp.toPx()
                }
                drawText(labels[index], x + barWidth / 2, height, paint)
            }
        }
    }
}

@Composable
fun HighlightCard(title: String, value: String, modifier: Modifier, color: Color) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .align(Alignment.TopStart)
                .background(VerdeNeon, CircleShape)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun HeatmapTable(data: Array<DoubleArray>) {
    val dias = listOf("D", "S", "T", "Q", "Q", "S", "S")
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

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
                        Text(text = dia, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.7f))
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
                        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.5f)
                    )
                    for (dia in 0..6) {
                        val valor = data[dia][hora]
                        val cellColor = when {
                            valor == 0.0 -> (if (isDark) Color.White else Color.Black).copy(alpha = 0.03f)
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
    val isDark = isSystemInDarkTheme()
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
            Text("Baixos", fontSize = 9.sp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f))
            Text("Médios", fontSize = 9.sp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f))
            Text("Ouro / Altos", fontSize = 9.sp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.4f))
        }
    }
}
