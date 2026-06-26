package com.raffastudioproducoes.minharota.ui.screens.caixas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
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

    Column(modifier = Modifier.fillMaxSize().background(FundoDark)) {
        // 1. BARRA DE FILTROS DE PERÍODO
        PeriodoSelector(selected = filtroPeriodo, onSelect = { viewModel.setFiltroPeriodo(it) })

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ALERTA DE PERCENTUAL
            erroPercentual?.let { erro ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                            Text(caixinha.nome, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            val percCalculado = (caixinha.percentual / 100.0) * ganhoLiquidoHoje
                            Text("R$ ${String.format("%.2f", percCalculado)} - ${caixinha.percentual.toInt()}% do dia", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
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
                                focusedLabelColor = VerdeNeon
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
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon)
                        ) {
                            Icon(Icons.Rounded.Savings, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar depósito", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. SEÇÃO CARD "VISÃO GERAL"
            item {
                Text(
                    text = "VISÃO GERAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(caixinhas) { caixinha ->
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
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(caixinhas) { caixinha ->
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Nova caixinha", color = Color.White.copy(alpha = 0.5f))
                        if (!isPro && caixinhas.size >= 3) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
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
                                    .background(if (isSelected) VerdeNeon else Color.White.copy(alpha = 0.05f))
                                    .clickable { viewModel.toggleDiaFolga(context, index + 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dia, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dias marcados são excluídos do cálculo da meta automática.",
                        color = Color.White.copy(alpha = 0.5f),
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
                    .background(if (isSelected) VerdeNeon else Color.White.copy(alpha = 0.05f))
                    .clickable { onSelect(periodo) },
                contentAlignment = Alignment.Center
            ) {
                Text(periodo, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 8.dp))
        PremiumGlassCard(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun MiniCardProgresso(caixinha: Caixinha, periodo: String, onClick: () -> Unit) {
    PremiumGlassCard(
        modifier = Modifier.width(160.dp).clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(caixinha.emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(caixinha.nome, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("R$ ${String.format("%.0f", caixinha.saldoAtual)} / $periodo", color = VerdeNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        val progresso = if (caixinha.metaValor > 0) (caixinha.saldoAtual / caixinha.metaValor).toFloat().coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = VerdeNeon,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
        Text("${(progresso * 100).toInt()}% · R$ ${String.format("%.0f", caixinha.saldoAtual)}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun GerenciarCaixinhaItem(caixinha: Caixinha, onUpdate: (Caixinha) -> Unit, onDelete: () -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    
    PremiumGlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(caixinha.emoji, modifier = Modifier.clickable { /* Mudar emoji */ })
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = caixinha.nome,
                onValueChange = { onUpdate(caixinha.copy(nome = it)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp)) }
            IconButton(onClick = { expandido = !expandido }) { Icon(if (expandido) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) }
        }
        
        if (expandido) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Percentual:", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.width(80.dp))
                Slider(
                    value = caixinha.percentual.toFloat(),
                    onValueChange = { onUpdate(caixinha.copy(percentual = it.toDouble())) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = VerdeNeon, activeTrackColor = VerdeNeon)
                )
                Text("${caixinha.percentual.toInt()}%", color = VerdeNeon, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Meta (R$):", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.width(80.dp))
                BasicTextField(
                    value = caixinha.metaValor.toString(),
                    onValueChange = { onUpdate(caixinha.copy(metaValor = it.toDoubleOrNull() ?: 0.0)) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}
