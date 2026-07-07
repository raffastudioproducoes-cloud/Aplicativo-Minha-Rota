package com.raffastudioproducoes.minharota.ui.screens.contas

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.domain.model.ContaFixa
import com.raffastudioproducoes.minharota.ui.components.AiInsightCard
import com.raffastudioproducoes.minharota.ui.components.CardConta
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.viewmodel.GeminiAiViewModel

@Composable
fun ContasScreen(
    viewModel: ContasViewModel = viewModel(),
    geminiViewModel: GeminiAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val contas by viewModel.contas.collectAsState()
    val metaDiaria by viewModel.metaDiariaAutomatica.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val faturamentoAnual by viewModel.faturamentoAnual.collectAsState()
    val limiteMei by viewModel.limiteMei.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ContaFixa?>(null) }
    var showPaywallModal by remember { mutableStateOf(false) }

    // Coletar estados do Gemini AI
    val contasInsight by geminiViewModel.contasInsight.collectAsState()
    val contasIsLoading by geminiViewModel.contasIsLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarContas(context)
    }

    // Disparar insight ao carregar (somente PRO — o ViewModel verifica internamente)
    LaunchedEffect(contas) {
        val contasPendentes = contas.filter { !it.paga }
        if (contasPendentes.isNotEmpty()) {
            val dividasInfo = contasPendentes.joinToString("\n") {
                "- ${it.nome}: R$ ${String.format("%.2f", it.valor)} (vence ${it.dataVencimento})"
            }
            geminiViewModel.gerarInsightContas(
                context = context,
                dividasInfo = dividasInfo
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // AI INSIGHT CARD (PRO) — topo absoluto da tela
        AiInsightCard(
            isPro = isPro,
            isLoading = contasIsLoading,
            insight = contasInsight,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Card de Destaque: Meta Diária Premium
        PremiumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Meta Diária Automática",
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R$ ${String.format("%.2f", metaDiaria)}",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VerdeNeon,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Baseado em suas contas fixas pendentes",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Minhas Contas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }

            val contasPendentes = contas.filter { !it.paga }
            val contasPagas = contas.filter { it.paga }

            items(contasPendentes) { conta ->
                CardConta(
                    nome = conta.nome,
                    dataVencimento = conta.dataVencimento,
                    valor = conta.valor,
                    pago = false,
                    onTogglePago = { viewModel.pagarConta(context, conta.id) },
                    onEdit = { showEditDialog = conta },
                    onDelete = { viewModel.excluirConta(context, conta.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.05f), contentColor = textColor)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Conta")
                }
            }

            if (contasPagas.isNotEmpty()) {
                item {
                    Text(
                        text = "Contas Pagas",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                }
                items(contasPagas) { conta ->
                    CardConta(
                        nome = conta.nome,
                        dataVencimento = conta.dataVencimento,
                        valor = conta.valor,
                        pago = true,
                        onTogglePago = { viewModel.pagarConta(context, conta.id) },
                        onEdit = { showEditDialog = conta },
                        onDelete = { viewModel.excluirConta(context, conta.id) }
                    )
                }
            }

            // SEÇÃO GESTÃO MEI PREMIUM
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "GESTÃO MEI",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                PremiumGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = VerdeNeon)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("FATURAMENTO ANUAL MEI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = textColor)
                            }
                            
                            val percentual = (faturamentoAnual / limiteMei * 100).coerceIn(0.0, 100.0)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(VerdeNeon.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${String.format("%.1f", percentual)}%", color = VerdeNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Faturado em 2026", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                Text("R$ ${String.format("%.0f", faturamentoAnual)}", color = VerdeNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Margem restante", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                Text("R$ ${String.format("%.0f", limiteMei - faturamentoAnual)}", color = VerdeNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val progresso = (faturamentoAnual / limiteMei).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = VerdeNeon,
                            trackColor = textColor.copy(alpha = 0.1f)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${String.format("%.1f", progresso * 100)}% do limite", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text("Teto: R$ ${String.format("%.0f", limiteMei)}", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val projecao = if (faturamentoAnual > 0) (faturamentoAnual / Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) * 365 else 0.0
                        Text(
                            text = "📈 Com a média atual, a projeção anual é R$ ${String.format("%.0f", projecao)} — ${if (projecao <= limiteMei) "dentro do limite MEI. ✅" else "atenção ao limite! ⚠️"}",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // DIALOGS
    if (showAddDialog) {
        ContaDialog(
            titulo = "Adicionar Conta",
            onDismiss = { showAddDialog = false },
            onConfirm = { nome, valor, venc ->
                viewModel.adicionarConta(context, nome, valor, venc)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { conta ->
        ContaDialog(
            titulo = "Editar Conta",
            contaInicial = conta,
            onDismiss = { showEditDialog = null },
            onConfirm = { nome, valor, venc ->
                viewModel.atualizarConta(context, conta.id, nome, valor, venc)
                showEditDialog = null
            }
        )
    }

    if (showPaywallModal) {
        com.raffastudioproducoes.minharota.ui.components.PaywallModal(
            onDismiss = { showPaywallModal = false },
            onUpgrade = { /* Implementar lógica de upgrade */ }
        )
    }
}

@Composable
fun ContaDialog(
    titulo: String,
    contaInicial: ContaFixa? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    var nome by remember { mutableStateOf(contaInicial?.nome ?: "") }
    var valor by remember { mutableStateOf(if (contaInicial != null) contaInicial.valor.toString() else "") }
    var vencimento by remember { mutableStateOf(contaInicial?.dataVencimento ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = textColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Conta") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor)
                )
                OutlinedTextField(
                    value = valor,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*[.,]?\\d*$"))) {
                            valor = input.replace(',', '.')
                        }
                    },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor)
                )
                OutlinedTextField(
                    value = vencimento,
                    onValueChange = { vencimento = it },
                    label = { Text("Vencimento (Ex: 20/06)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val v = valor.toDoubleOrNull() ?: 0.0
                    if (nome.isNotBlank() && v > 0) {
                        onConfirm(nome, v, vencimento)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = textColor.copy(alpha = 0.5f)) }
        },
        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
