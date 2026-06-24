package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RegisterScreen(onNavigateBack: () -> Unit) {
    // Fundo Carbono Profundo Absoluto (Color(0xFF0C0C0E))
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0C0C0E)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tela de Cadastro\n(Em Construção)",
                color = Color.White.copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )
        }
    }
}
