package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente reutilizável de Card com efeito Glassmorphism para exibir insights de IA.
 *
 * - Apenas renderiza se o usuário for PRO (FREE: sem card, sem chamada de rede)
 * - Exibe shimmer animado enquanto a chamada ao Gemini está em andamento
 * - Mostra resposta contextual com fade-in quando pronta
 * - Respeita o tema global (escuro translúcido / claro translúcido jateado)
 * - Borda reflexiva gradiente de 1.dp, ícone Emerald Green
 *
 * @param isPro    Se false, o card inteiro não é renderizado
 * @param isLoading Se true, exibe Shimmer; se false, mostra o insight
 * @param insight  Texto do insight gerado pela IA
 * @param modifier Modificador customizável
 */
@Composable
fun AiInsightCard(
    isPro: Boolean,
    isLoading: Boolean,
    insight: String,
    modifier: Modifier = Modifier
) {
    // Usuários FREE: sem card, sem chamada de rede
    if (!isPro) return

    val isDark = isSystemInDarkTheme()

    // Glassmorphism: translúcido adaptativo ao tema
    val glassBackground = if (isDark)
        Color.White.copy(alpha = 0.06f)
    else
        Color.Black.copy(alpha = 0.04f)

    // Borda reflexiva gradiente de 1.dp
    val borderGradient = if (isDark)
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.05f),
                Color(0xFF42D789).copy(alpha = 0.30f)
            )
        )
    else
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.10f),
                Color.Black.copy(alpha = 0.03f),
                Color(0xFF42D789).copy(alpha = 0.20f)
            )
        )

    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val emeraldGreen = Color(0xFF42D789) // Emerald Green para o ícone

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassBackground)
            .border(
                width = 1.dp,
                brush = borderGradient,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header: ícone + label ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Insight IA",
                    tint = emeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Insight IA · Gemini",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = emeraldGreen,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // ── Conteúdo: Shimmer OU Texto ────────────────────────────────
            if (isLoading) {
                AiInsightShimmer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            } else {
                AnimatedVisibility(
                    visible = insight.isNotBlank(),
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    )
                }
            }
        }
    }
}

/**
 * Efeito Shimmer (carregamento) com animação de pulso infinita.
 * Respeita o tema claro/escuro sem usar imagens de fundo.
 */
@Composable
fun AiInsightShimmer(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()

    val infiniteTransition = rememberInfiniteTransition(label = "ai_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    val shimmerBase = if (isDark)
        Color.White.copy(alpha = alpha * 0.15f)
    else
        Color.Black.copy(alpha = alpha * 0.08f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Linha 1
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(shimmerBase)
        )
        // Linha 2
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(shimmerBase)
        )
    }
}

/**
 * Alias de compatibilidade — mantido para não quebrar referências antigas.
 */
@Composable
fun ShimmerLoadingEffect(modifier: Modifier = Modifier) = AiInsightShimmer(modifier)
