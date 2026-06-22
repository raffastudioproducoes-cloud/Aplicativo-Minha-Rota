package com.raffastudioproducoes.minharota.ui.screens.contas

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.domain.model.ContaFixa
import com.raffastudioproducoes.minharota.ui.components.CardConta
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada

@Composable
fun ContasScreen(viewModel: ContasViewModel = viewModel()) {
    val context = LocalContext.current
    val contas by viewModel.contas.collectAsState()
    val metaDiaria by viewModel.metaDiariaAutomatica.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val faturamentoAnual by viewModel.faturamentoAnual.collectAsState()
    val limiteMei by viewModel.limiteMei.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ContaFixa?>(null) }
    var showPaywallModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.carregarContas(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        // Card de Destaque: Meta Diária
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E20)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Meta Diária Automática",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R$ ${String.format("%.2f", metaDiaria)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VerdeEntrada
                )
                Text(
                    text = "Baseado em suas contas fixas pendentes",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.6f)
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
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Conta", color = Color.White)
                }
            }

            if (contasPagas.isNotEmpty()) {
                item {
                    Text(
                        text = "Contas Pagas",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
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

            // SEÇÃO GESTÃO MEI
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "GESTÃO MEI",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121214)),
                    onClick = { if (!isPro) showPaywallModal = true }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = VerdeEntrada)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("FATURAMENTO ANUAL MEI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            
                            val percentual = (faturamentoAnual / limiteMei * 100).coerceIn(0.0, 100.0)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(VerdeEntrada.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${String.format("%.1f", percentual)}%", color = VerdeEntrada, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Faturado em 2026", color = Color.Gray, fontSize = 12.sp)
                                Text("R$ ${String.format("%.0f", faturamentoAnual)}", color = VerdeEntrada, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Margem restante", color = Color.Gray, fontSize = 12.sp)
                                Text("R$ ${String.format("%.0f", limiteMei - faturamentoAnual)}", color = VerdeEntrada, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val progresso = (faturamentoAnual / limiteMei).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = VerdeEntrada,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${String.format("%.1f", progresso * 100)}% do limite", color = Color.Gray, fontSize = 10.sp)
                            Text("Teto: R$ ${String.format("%.0f", limiteMei)}", color = Color.Gray, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val projecao = if (faturamentoAnual > 0) (faturamentoAnual / Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) * 365 else 0.0
                        Text(
                            text = "📈 Com a média atual, a projeção anual é R$ ${String.format("%.0f", projecao)} — ${if (projecao <= limiteMei) "dentro do limite MEI. ✅" else "atenção ao limite! ⚠️"}",
                            color = Color.Gray,
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
    var nome by remember { mutableStateOf(contaInicial?.nome ?: "") }
    var valor by remember { mutableStateOf(if (contaInicial != null) contaInicial.valor.toString() else "") }
    var vencimento by remember { mutableStateOf(contaInicial?.dataVencimento ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome da Conta") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = valor, onValueChange = { valor = it }, label = { Text("Valor (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vencimento, onValueChange = { vencimento = it }, label = { Text("Vencimento (Ex: 20/06)") }, modifier = Modifier.fillMaxWidth())
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
                colors = ButtonDefaults.buttonColors(containerColor = VerdeEntrada)
            ) {
                Text("Salvar", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
