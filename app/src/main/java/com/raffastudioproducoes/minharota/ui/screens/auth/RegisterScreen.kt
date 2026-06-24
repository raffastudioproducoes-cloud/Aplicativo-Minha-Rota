package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.raffastudioproducoes.minharota.ui.theme.FundoDark

@Composable
fun RegisterScreen(onNavigateBack: () -> Unit) {
    // Tela em branco conforme solicitado v1.9.0
    // Fundo Carbono Profundo Absoluto
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDark),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tela de Cadastro\n(Em Construção)",
            color = Color.White.copy(alpha = 0.3f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
