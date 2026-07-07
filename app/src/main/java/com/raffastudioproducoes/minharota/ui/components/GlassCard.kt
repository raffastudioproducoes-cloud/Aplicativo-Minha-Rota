package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val backgroundColor = if (isDark) Color(0x26FFFFFF) else Color(0x66FFFFFF)
    val borderColor = if (isDark) 
        Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent))
        else Brush.linearGradient(colors = listOf(Color.White, Color.Transparent))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // AQUI ADICIONAMOS O ESPAÇAMENTO:
        // O conteúdo fica preso dentro desta Box que tem o padding interno
        Box(modifier = Modifier.padding(16.dp)) { 
            content()
        }
    }
}
