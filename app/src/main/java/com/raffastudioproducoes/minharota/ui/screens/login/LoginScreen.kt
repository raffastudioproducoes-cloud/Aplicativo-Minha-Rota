package com.raffastudioproducoes.minharota.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada

@Composable
fun LoginScreen(onNavigateToDashboard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bem-vindo ao MinhaRota",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Faça login para continuar",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botão Google
        Button(
            onClick = { if (FirebaseAuth.getInstance().currentUser != null) onNavigateToDashboard() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            )
        ) {
            Text(
                text = "Continuar com o Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão Apple
        Button(
            onClick = { if (FirebaseAuth.getInstance().currentUser != null) onNavigateToDashboard() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(
                text = "Continuar com a Apple",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Botão Planos Premium
        Button(
            onClick = { /* TODO: Navegar para tela de Planos */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeEntrada
            )
        ) {
            Text(
                text = "Ver Planos Premium",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
