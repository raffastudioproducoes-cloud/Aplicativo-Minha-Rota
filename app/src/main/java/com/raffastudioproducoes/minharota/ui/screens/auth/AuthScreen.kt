package com.raffastudioproducoes.minharota.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.raffastudioproducoes.minharota.R
import com.raffastudioproducoes.minharota.domain.model.User
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToEmailLogin: () -> Unit,
    userViewModel: UserViewModel
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

    var isSigningIn by remember { mutableStateOf(false) }

    fun handleSocialLogin(
        providerId: String,
        userViewModel: UserViewModel,
        onAuthSuccess: () -> Unit
    ) {
        if (providerId == "apple.com") {
            showAppleDialog = true
            return
        }

        if (providerId == "google.com") {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("511340037072-84g8p17t2mi8idosurripn1vi9o2221f.apps.googleusercontent.com") // <--- COLE O ID DO FIREBASE AQUI
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            scope.launch {
                try {
                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential

                    if (credential is GoogleIdTokenCredential) {
                        val firebaseCredential =
                            GoogleAuthProvider.getCredential(credential.idToken, null)

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // MUDANÇA: Use o resultado da tarefa para pegar o usuário
                                    val firebaseUser = task.result.user
                                    if (firebaseUser != null) {
                                        val user = User(
                                            uid = firebaseUser.uid,
                                            displayName = firebaseUser.displayName ?: "Usuário",
                                            email = firebaseUser.email ?: "",
                                            photoUrl = firebaseUser.photoUrl?.toString()
                                        )
                                        // Navega apenas dentro do callback de sucesso do salvamento
                                        // Chama o ViewModel que você já definiu anteriormente
                                        userViewModel.registerOrUpdateUser(user) {
                                            isSigningIn = false
                                            onAuthSuccess()
                                        }
                                    }
                                } else {
                                    isSigningIn = false
                                    Log.e("AuthScreen", "Erro Firebase: ${task.exception?.message}")
                                    Toast.makeText(
                                        context,
                                        "Erro: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } catch (e: Exception) {
                    // 1. Log detalhado para o desenvolvedor (Aparece no Logcat do Android Studio)
                    // O 'e' no final faz o Android imprimir o Stack Trace completo.
                    Log.e("AuthScreen", "--- ERRO DE LOGIN ---", e)
                    Log.e("AuthScreen", "Mensagem: ${e.message}")
                    Log.e("AuthScreen", "Causa: ${e.cause}")

                    // 2. Feedback amigável para o usuário (Não mostre stack trace para o usuário final!)
                    val mensagemErro = when {
                        e.message?.contains("10") == true -> "Erro de configuração (Google API Code 10)"
                        e.message?.contains("Canceled") == true -> "Login cancelado"
                        else -> "Erro: ${e.localizedMessage}"
                    }
                    Toast.makeText(context, mensagemErro, Toast.LENGTH_LONG).show()
                }
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
                    modifier = Modifier
                        .size(180.dp)
                        .padding(bottom = 24.dp)
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
                    onClick = {
                        isSigningIn = true // Bloqueia o botão e mostra loading
                        handleSocialLogin(
                            "google.com",
                            userViewModel = userViewModel, // O ViewModel que você está usando na tela
                            onAuthSuccess = onAuthSuccess  // A função de navegação
                        )
                    },
                    enabled = !isSigningIn, // Desabilita enquanto carrega
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color(0xFFF3F4F6),
                        contentColor = Color.Black
                    )
                ) {
                    if (isSigningIn) CircularProgressIndicator(Modifier.size(20.dp))
                    else
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continuar com Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        handleSocialLogin(
                            "apple.com",
                            userViewModel = userViewModel, // O ViewModel que você está usando na tela
                            onAuthSuccess = onAuthSuccess  // A função de navegação
                        )
                    },
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
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    cianoNeon,
                                    verdeEsmeralda
                                )
                            ),
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
                                modifier = Modifier
                                    .size(140.dp)
                                    .padding(bottom = 24.dp)
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
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            cianoNeon,
                                            verdeEsmeralda
                                        )
                                    ),
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
                        .background(Color(0xfd121214))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.0f),
                                    Color.Transparent
                                )
                            ),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
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
