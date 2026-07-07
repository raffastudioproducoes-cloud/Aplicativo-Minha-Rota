package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente reutilizável de Card com efeito Glassmorphism para exibir insights de IA.
 * 
 * - Apenas renderiza se o usuário for PRO
 * - Exibe shimmer enquanto carrega
 * - Mostra resposta contextual quando pronta
 * 
 * @param isPro Verifica se o usuário é PRO (FREE não renderiza nada)
 * @param isLoading Se true, exibe Shimmer; se false, mostra o insight
 * @param insight Texto do insight gerado pela IA
 * @param modifier Modificador customizável
 */
@Composable
fun AiInsightCard(
    isPro: Boolean,
    isLoading: Boolean,
    insight: String,
    modifier: Modifier = Modifier
) {
    // Se não é PRO, não renderiza nada
    if (!isPro) return

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val emeraldGreen = Color(0xFF42D789) // Emerald Green para o ícone

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header com Ícone + Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "IA Insight",
                    tint = emeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Insight IA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 12.sp
                )
            }

            // Conteúdo: Shimmer OU Texto
            if (isLoading) {
                ShimmerLoadingEffect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            } else {
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Efeito Shimmer (Carregamento) com animação infinita
 */
@Composable
fun ShimmerLoadingEffect(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val shimmerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(shimmerColor.copy(alpha = alpha))
    )
}
