package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun CardCaixinha(
    emoji: String,
    titulo: String,
    valorGuardado: Double,
    metaValor: Double,
    corDestaque: Color,
    percentual: Float = 0f,
    onDepositoClick: (() -> Unit)? = null
) {
    val progresso = if (metaValor > 0) (valorGuardado / metaValor).toFloat().coerceIn(0f, 1f) else 0f

    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "R$ ${String.format("%.2f", valorGuardado)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerdeNeon,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (metaValor > 0) {
                Text(
                    text = "${(progresso * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        if (metaValor > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progresso,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = VerdeNeon,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}
