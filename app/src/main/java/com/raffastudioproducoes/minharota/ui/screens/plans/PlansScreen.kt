package com.raffastudioproducoes.minharota.ui.screens.plans

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.ui.components.CheckoutModal
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ViewModel local para gerenciar status de assinatura de forma reativa
class PlansViewModel : ViewModel() {
    private val _planoAtual = MutableStateFlow("free") // "free", "premium", "pro"
    val planoAtual: StateFlow<String> = _planoAtual.asStateFlow()

    fun carregarPlano(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _planoAtual.value = if (prefs.obterIsPro()) "premium" else "free"
    }

    // Mock de retorno positivo do Google Play Billing
    fun escolherPlano(plano: String, context: Context) {
        val prefs = SharedPreferencesManager(context)
        _planoAtual.value = plano
        // Persiste localmente — simula confirmação do billing
        prefs.salvarIsPro(plano != "free")
    }

    fun cancelarPlano(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _planoAtual.value = "free"
        prefs.salvarIsPro(false)
    }
}

@Composable
fun PlansScreen(plansViewModel: PlansViewModel = viewModel()) {
    val context = LocalContext.current
    val planoAtual by plansViewModel.planoAtual.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    // Estado do modal de checkout
    var checkoutPlano by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        plansViewModel.carregarPlano(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Escolha seu Plano",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (planoAtual != "free") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Plano ${planoAtual.replaceFirstChar { it.uppercase() }} ativo — recursos premium liberados!",
                    fontSize = 13.sp,
                    color = VerdeEntrada,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Plano Free
        PlanCard(
            titulo = "Free",
            preco = "Grátis",
            descricao = "Perfeito para começar",
            recursos = listOf(
                "Gestão de Turnos",
                "Caixinhas Básicas",
                "Histórico de 30 dias",
                "Suporte por Email"
            ),
            ehAtual = planoAtual == "free",
            destaque = false,
            onEscolher = { plansViewModel.escolherPlano("free", context) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Plano Premium
        PlanCard(
            titulo = "Premium",
            preco = "R$ 9,99/mês",
            descricao = "Para profissionais",
            recursos = listOf(
                "Tudo do Free +",
                "Heatmap de Horários de Ouro",
                "Análise Avançada",
                "Histórico Ilimitado",
                "Suporte Prioritário",
                "OCR de Documentos"
            ),
            ehAtual = planoAtual == "premium",
            destaque = true,
            onEscolher = { checkoutPlano = "Premium" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Plano Pro
        PlanCard(
            titulo = "Pro",
            preco = "R$ 19,99/mês",
            descricao = "Para empresas",
            recursos = listOf(
                "Tudo do Premium +",
                "Gestão de Múltiplos Motoristas",
                "Relatórios Personalizados",
                "API de Integração",
                "Suporte 24/7",
                "Backup Automático"
            ),
            ehAtual = planoAtual == "pro",
            destaque = false,
            onEscolher = { checkoutPlano = "Pro" }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Modal de Checkout Glassmorphic
    checkoutPlano?.let { plano ->
        CheckoutModal(
            nomePlano = plano,
            onDismiss = { checkoutPlano = null },
            onSuccess = {
                plansViewModel.escolherPlano(plano.lowercase(), context)
                checkoutPlano = null
            }
        )
    }
}

@Composable
fun PlanCard(
    titulo: String,
    preco: String,
    descricao: String,
    recursos: List<String>,
    ehAtual: Boolean = false,
    destaque: Boolean = false,
    onEscolher: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (destaque) {
                    Modifier.border(
                        width = 2.dp,
                        color = VerdeEntrada,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (destaque) cardColor.copy(alpha = 0.8f) else cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = descricao,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (ehAtual) {
                    Surface(
                        color = VerdeEntrada,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Atual",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Text(
                text = preco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeEntrada,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Lista de Recursos
            recursos.forEach { recurso ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 12.dp),
                        tint = VerdeEntrada
                    )
                    Text(
                        text = recurso,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de Ação — reativo ao status atual
            Button(
                onClick = onEscolher,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (ehAtual) Color.Gray else VerdeEntrada
                ),
                shape = RoundedCornerShape(50.dp),
                enabled = !ehAtual
            ) {
                Text(
                    text = if (ehAtual) "Plano Atual" else "Escolher Plano",
                    fontWeight = FontWeight.Bold,
                    color = if (ehAtual) Color.White else Color.Black
                )
            }
        }
    }
}
