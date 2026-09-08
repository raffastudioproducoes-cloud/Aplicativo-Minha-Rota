package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.theme.isAppDarkTheme
import kotlin.math.sin

/**
 * Card de resumo estilo "Analytics" (fintech): valor grande + mini gráfico ondulado.
 * Puramente decorativo — a onda representa a variação do ganho ao longo dos últimos dias.
 */
@Composable
fun AnalyticsSummaryCard(
    titulo: String,
    valorFormatado: String,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    PremiumGlassCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = titulo.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = valorFormatado,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(12.dp))
        WaveLine(modifier = Modifier.fillMaxWidth().height(48.dp))
    }
}

@Composable
private fun WaveLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        val path = Path()
        val points = 60
        for (i in 0..points) {
            val x = width * (i / points.toFloat())
            val y = midY + sin(i * 0.35f) * (height * 0.28f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = VerdeNeon,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // ponto de destaque no meio da onda
        val highlightX = width * 0.62f
        val highlightY = midY + sin(0.35f * (points * 0.62f)) * (height * 0.28f)
        drawCircle(color = VerdeNeon, radius = 6f, center = Offset(highlightX, highlightY))
        drawCircle(
            color = VerdeNeon.copy(alpha = 0.25f),
            radius = 12f,
            center = Offset(highlightX, highlightY)
        )
    }
}
