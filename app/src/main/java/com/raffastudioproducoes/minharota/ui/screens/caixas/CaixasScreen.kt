package com.raffastudioproducoes.minharota.ui.screens.caixas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.raffastudioproducoes.minharota.domain.model.Caixinha
import com.raffastudioproducoes.minharota.ui.components.PaywallModal
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun CaixasScreen(
    viewModel: CaixasViewModel = viewModel(),
    hojeViewModel: HojeViewModel = viewModel()
) {
    val context = LocalContext.current
    val caixinhas by viewModel.caixinhas.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val showPaywallModal by viewModel.showPaywallModal.collectAsState()
    val filtroPeriodo by viewModel.filtroPeriodo.collectAsState()
    val diasFolga by viewModel.diasFolga.collectAsState()
    val erroPercentual by viewModel.erroPercentual.collectAsState()
    val ganhoLiquidoHoje by hojeViewModel.ganhoLiquido.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    var caixinhaSelecionadaId by remember { mutableStateOf<String?>(null) }
    var valorDepositoManual by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.carregarDados(context)
    }

    LaunchedEffect(caixinhas) {
        if (caixinhas.isNotEmpty() && caixinhaSelecionadaId == null) {
            caixinhaSelecionadaId = caixinhas.first().id
        }
    }

    val caixinhaSelecionada = caixinhas.find { it.id == caixinhaSelecionadaId }
    val saldoTotalGuardado = caixinhas.sumOf { it.saldoAtual }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 1. BARRA DE FILTROS DE PERÍODO
        PeriodoSelector(selected = filtroPeriodo, onSelect = { viewModel.setFiltroPeriodo(it) })

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // NOVO CARD: SALDO TOTAL GUARDADO
            item {
                PremiumGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SALDO TOTAL GUARDADO",
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "R$ ${String.format("%.2f", saldoTotalGuardado)}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = VerdeNeon,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Atualizado apenas após confirmação de depósito",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ALERTA DE PERCENTUAL
            erroPercentual?.let { erro ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B).copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF991B1B))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(erro, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. SEÇÃO CARD "DEPÓSITO DE HOJE"
            item {
                SectionCard(title = "DEPÓSITO DE HOJE") {
                    caixinhaSelecionada?.let { caixinha ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(caixinha.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(caixinha.nome, color = textColor.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            val percCalculado = (caixinha.percentual / 100.0) * ganhoLiquidoHoje
                            Text("R$ ${String.format("%.2f", percCalculado)} - ${caixinha.percentual.toInt()}% do dia", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = valorDepositoManual,
                            onValueChange = { valorDepositoManual = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Total guardado (R$)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdeNeon,
                                focusedLabelColor = VerdeNeon,
                                unfocusedTextColor = textColor,
                                focusedTextColor = textColor
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                val valor = valorDepositoManual.toDoubleOrNull() ?: 0.0
                                if (valor > 0) {
                                    viewModel.confirmarDeposito(context, caixinha.id, valor)
                                    valorDepositoManual = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Rounded.Savings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar depósito", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. SEÇÃO CARD "VISÃO GERAL"
            item {
                Text(
                    text = "VISÃO GERAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(caixinhas, key = { it.id }) { caixinha ->
                        MiniCardProgresso(caixinha, filtroPeriodo) { caixinhaSelecionadaId = caixinha.id }
                    }
                }
            }

            // 4. SEÇÃO "GERENCIAR CAIXINHAS"
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "GERENCIAR CAIXINHAS",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(caixinhas, key = { "manage-${it.id}" }) { caixinha ->
                GerenciarCaixinhaItem(
                    caixinha = caixinha,
                    onUpdate = { viewModel.atualizarCaixinha(context, it) },
                    onDelete = { viewModel.excluirCaixinha(context, caixinha.id) }
                )
            }

            item {
                val totalAlocado = caixinhas.sumOf { it.percentual }
                Text(
                    text = "Total alocado: ${totalAlocado.toInt()}%",
                    color = if (totalAlocado > 100) Color.Red else VerdeNeon,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { viewModel.adicionarCaixinha(context) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = textColor.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Nova caixinha", color = textColor.copy(alpha = 0.5f))
                        if (!isPro && caixinhas.size >= 3) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // 5. SEÇÃO PREMIUM "DIAS DE FOLGA FIXOS"
            item {
                Spacer(modifier = Modifier.height(32.dp))
                SectionCard(title = "DIAS DE FOLGA FIXOS") {
                    val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        diasSemana.forEachIndexed { index, dia ->
                            val isSelected = diasFolga.contains(index + 1)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) VerdeNeon else textColor.copy(alpha = 0.05f))
                                    .clickable { viewModel.toggleDiaFolga(context, index + 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dia, color = if (isSelected) Color.Black else textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dias marcados são excluídos do cálculo da meta automática.",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showPaywallModal) {
        PaywallModal(onDismiss = { viewModel.dismissPaywallModal() }, onUpgrade = { viewModel.upgradeToPro(context) })
    }
}

@Composable
fun PeriodoSelector(selected: String, onSelect: (String) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    val periodos = listOf("Hoje", "Semana", "Mês", "Ano")
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periodos.forEach { periodo ->
            val isSelected = selected == periodo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) VerdeNeon else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)))
                    .clickable { onSelect(periodo) },
                contentAlignment = Alignment.Center
            ) {
                Text(periodo, color = if (isSelected) Color.Black else textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 8.dp))
        PremiumGlassCard(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun MiniCardProgresso(caixinha: Caixinha, periodo: String, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val progresso = if (caixinha.metaValor > 0) (caixinha.saldoAtual / caixinha.metaValor).toFloat().coerceIn(0f, 1f) else 0f
    
    PremiumGlassCard(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Text(caixinha.emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(caixinha.nome, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
        Text("R$ ${String.format("%.2f", caixinha.saldoAtual)}", color = VerdeNeon, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = progresso,
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = VerdeNeon,
            trackColor = textColor.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun GerenciarCaixinhaItem(
    caixinha: Caixinha,
    onUpdate: (Caixinha) -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    var showEditDialog by remember { mutableStateOf(false) }
    var editNome by remember { mutableStateOf(caixinha.nome) }
    var editPercent by remember { mutableStateOf(caixinha.percentual.toString()) }
    var editMeta by remember { mutableStateOf(caixinha.metaValor.toString()) }

    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(caixinha.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(caixinha.nome, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${caixinha.percentual.toInt()}% do faturamento diário", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Caixinha", color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNome,
                        onValueChange = { editNome = it },
                        label = { Text("Nome da Caixinha") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor)
                    )
                    OutlinedTextField(
                        value = editPercent,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) editPercent = it.replace(',', '.') },
                        label = { Text("Percentual (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor)
                    )
                    OutlinedTextField(
                        value = editMeta,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) editMeta = it.replace(',', '.') },
                        label = { Text("Meta Final (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdate(caixinha.copy(
                            nome = editNome,
                            percentual = editPercent.toDoubleOrNull() ?: caixinha.percentual,
                            metaValor = editMeta.toDoubleOrNull() ?: caixinha.metaValor
                        ))
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = textColor.copy(alpha = 0.5f))
                }
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
