package com.raffastudioproducoes.minharota.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.raffastudioproducoes.minharota.R
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToEmailLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val isDark = isSystemInDarkTheme()

    // Cores Neon
    val cianoNeon = Color(0xFF22D3EE)
    val verdeEsmeralda = Color(0xFF10B981)
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    fun handleSocialLogin(providerId: String) {
        scope.launch {
            try {
                if (providerId == "google.com") {
                    Toast.makeText(context, "Google Login: SHA-1 e Firebase Console necessários.", Toast.LENGTH_LONG).show()
                } else if (providerId == "apple.com") {
                    val provider = OAuthProvider.newBuilder("apple.com")
                    provider.scopes = listOf("email", "name")
                    
                    auth.startActivityForSignInWithProvider(context as android.app.Activity, provider.build())
                        .addOnSuccessListener { onAuthSuccess() }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro Apple: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: Exception) {
                Log.e("AuthScreen", "Erro Social Login", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Master
            Image(
                painter = painterResource(id = R.drawable.app_icon_master),
                contentDescription = "MinhaRota PRO Logo Master",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 24.dp)
            )

            // Título de Boas-vindas
            Text(
                text = "Bem-vindo ao Futuro",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtítulo
            Text(
                text = "Sua jornada, seu controle financeiro.\nFaça login para continuar.",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Botão Google
            Button(
                onClick = { handleSocialLogin("google.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color.White else Color(0xFFF3F4F6),
                    contentColor = Color.Black
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuar com Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Apple
            Button(
                onClick = { handleSocialLogin("apple.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF1A1A1A) else Color.Black,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_apple_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuar com Apple",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de Visitante
            Button(
                onClick = onAuthSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(colors = listOf(cianoNeon, verdeEsmeralda)),
                        shape = RoundedCornerShape(50.dp)
                    ),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Entrar como Visitante",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link: Entrar com e-mail
            Text(
                text = "Entrar com o e-mail",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onNavigateToEmailLogin() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Rodapé de Navegação
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Não tem uma conta? ",
                    color = textColor.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Cadastrar",
                    color = VerdeNeon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
