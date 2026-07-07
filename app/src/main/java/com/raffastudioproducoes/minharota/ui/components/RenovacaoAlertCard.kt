package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Card de alerta de renovação do plano.
 * Exibido no topo da HojeScreen quando faltam 10 dias ou menos para o vencimento.
 * Estilo Glassmorphism com borda sutilmente amarelada/laranja para atenção.
 */
@Composable
fun RenovacaoAlertCard(
    nomePlano: String,
    diasRestantes: Long,
    onRenovar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    // Cor de atenção: amarelo-laranja
    val alertColor = when {
        diasRestantes <= 3 -> Color(0xFFEF4444) // vermelho urgente
        diasRestantes <= 7 -> Color(0xFFF97316) // laranja
        else -> Color(0xFFFBBF24)               // amarelo
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 })
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            alertColor.copy(alpha = if (isDark) 0.18f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            alertColor.copy(alpha = 0.7f),
                            alertColor.copy(alpha = 0.15f),
                            alertColor.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = alertColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Plano $nomePlano",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = alertColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val textoVencimento = when (diasRestantes) {
                    0L -> "Seu plano $nomePlano vence hoje!"
                    1L -> "Seu plano $nomePlano vence amanhã."
                    else -> "Seu plano $nomePlano vence em $diasRestantes dias."
                }
                Text(
                    text = "$textoVencimento Renove agora para não perder seus recursos e históricos exclusivos!",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRenovar,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = alertColor,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Renovar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
