package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.raffastudioproducoes.minharota.R
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo MinhaRota PRO
            Image(
                painter = painterResource(id = R.drawable.logo_minha_rota_pro),
                contentDescription = "MinhaRota PRO Logo",
                modifier = Modifier
                    .size(240.dp)
                    .padding(bottom = 32.dp)
            )

            // Subtítulo
            Text(
                text = "Sua jornada, seu controle financeiro.\nFaça login para continuar.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Botão Google (Estilizado sem ícone vetorial problemático)
            Button(
                onClick = { /* Implementar OAuth Google */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Continuar com Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Apple
            Button(
                onClick = { /* Implementar OAuth Apple */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = " Continuar com Apple",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divisor Visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                Text(
                    text = "ou",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão de Demonstração (Visitante)
            Button(
                onClick = onAuthSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeEntrada.copy(alpha = 0.1f),
                    contentColor = VerdeEntrada
                )
            ) {
                Text(
                    text = "Entrar como Visitante",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Texto de Termos
            Text(
                text = "Ao continuar, você concorda com nossos\nTermos de Serviço e Política de Privacidade",
                fontSize = 11.sp,
                color = Color.Gray.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
