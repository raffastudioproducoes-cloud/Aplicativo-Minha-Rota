package com.raffastudioproducoes.minharota.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
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
    
    // Gerenciador da primeira visita
    val prefs = remember { context.getSharedPreferences("minha_rota_prefs", Context.MODE_PRIVATE) }
    var showOnboardingCard by remember { mutableStateOf(prefs.getBoolean("isFirstRun", true)) }
    
    var showAppleDialog by remember { mutableStateOf(false) }

    // Cores
    val cianoNeon = Color(0xFF22D3EE)
    val verdeEsmeralda = Color(0xFF10B981)
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    fun handleSocialLogin(providerId: String) {
        if (providerId == "apple.com") {
            showAppleDialog = true
            return
        }
        
        scope.launch {
            try {
                if (providerId == "google.com") {
                    Toast.makeText(context, "Google Login: SHA-1 e Firebase Console necessários.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("AuthScreen", "Erro Social Login", e)
            }
        }
    }

    // BOX MESTRE - Segura tudo na tela
    Box(modifier = Modifier.fillMaxSize()) {
        
        // 1. TELA DE LOGIN (Fica borrada se o Card Suspenso estiver aberto)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(if (showOnboardingCard) Modifier.blur(16.dp) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon_master),
                    contentDescription = "MinhaRota PRO Logo Master",
                    modifier = Modifier.size(180.dp).padding(bottom = 24.dp)
                )

                Text(
                    text = "Bem-vindo ao Futuro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Sua jornada, seu controle financeiro.\nFaça login para continuar.",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Button(
                    onClick = { handleSocialLogin("google.com") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color(0xFFF3F4F6),
                        contentColor = Color.Black
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continuar com Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { handleSocialLogin("apple.com") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1A1A1A) else Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_apple_logo), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continuar com Apple", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black)
                ) {
                    Text("Entrar como Visitante", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Entrar com o e-mail",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToEmailLogin() }
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Não tem uma conta? ", color = textColor.copy(alpha = 0.4f), fontSize = 14.sp)
                    Text("Cadastrar", color = VerdeNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
                }
            }
        }

        // 2. CARD SUSPENSO DE APRESENTAÇÃO (Aparece apenas na 1ª vez)
        if (showOnboardingCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)) // Borrão escuro cobrindo a tela
                    .padding(horizontal = 32.dp, vertical = 48.dp), // Espaçamento de ~1cm das bordas
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White // 100% visível/sólido
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Título do Card
                        Text(
                            text = "MinhaRota PRO",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                        )

                        // Conteúdo Rolável (Caso você adicione muita informação no futuro)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_master), // Pode trocar a imagem depois
                                contentDescription = "Apresentação",
                                modifier = Modifier.size(140.dp).padding(bottom = 24.dp)
                            )
                            
                            Text(
                                text = "A Revolução do seu Dia a Dia",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeNeon,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Text(
                                text = "Bem-vindo ao sistema de gestão financeira definitivo. \n\nCom o MinhaRota, você tem controle total de gastos, metas e ganhos diários na palma da mão, com um visual moderno e IA integrada.",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }

                        // Botão de "Começar" colado embaixo
                        Button(
                            onClick = {
                                // Salva que já viu o onboarding e fecha o card
                                prefs.edit().putBoolean("isFirstRun", false).apply()
                                showOnboardingCard = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    brush = Brush.horizontalGradient(colors = listOf(cianoNeon, verdeEsmeralda)),
                                    shape = RoundedCornerShape(50.dp)
                                ),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black)
                        ) {
                            Text("Começar Agora", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // 3. Modal Glassmorphism "Em Breve" (Login Apple)
        if (showAppleDialog) {
            Dialog(onDismissRequest = { showAppleDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x1F121214))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Em Breve", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("O login com a Apple estará disponível nas próximas atualizações.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showAppleDialog = false },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Entendi", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
