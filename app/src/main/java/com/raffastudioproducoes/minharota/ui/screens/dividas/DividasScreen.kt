package com.raffastudioproducoes.minharota.ui.screens.dividas

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.raffastudioproducoes.minharota.domain.model.Divida
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun DividasScreen(viewModel: DividasViewModel = viewModel()) {
    val context = LocalContext.current
    val dividas by viewModel.dividas.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    
    var mostrarDialogoPagamento by remember { mutableStateOf(false) }
    var mostrarDialogoFormDivida by remember { mutableStateOf(false) }
    var dividaSelecionada by remember { mutableStateOf<Divida?>(null) }
    var valorPagamento by remember { mutableStateOf("") }
    
    val corEmAberto = Color(0xFFFB7185)
    val corTotalPago = Color(0xFF34D399)

    LaunchedEffect(Unit) {
        viewModel.carregarDividas(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text(
            text = "Minhas Dívidas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Em aberto", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R$ ${String.format("%.2f", dividas.sumOf { it.valorTotal - it.valorPago })}",
                        color = corEmAberto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Total Pago", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R$ ${String.format("%.2f", dividas.sumOf { it.valorPago })}",
                        color = corTotalPago,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(dividas) { divida ->
                CardDivida(
                    divida = divida,
                    onAbrirDialogoPagamento = { 
                        dividaSelecionada = divida
                        valorPagamento = ""
                        mostrarDialogoPagamento = true
                    },
                    onQuitarDivida = { id -> viewModel.quitarDivida(context, id) },
                    onExcluir = { id -> viewModel.excluirDivida(context, id) },
                    onEditar = {
                        dividaSelecionada = divida
                        mostrarDialogoFormDivida = true
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        dividaSelecionada = null
                        mostrarDialogoFormDivida = true 
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.05f), contentColor = textColor)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Dívida")
                }
            }
        }
    }

    if (mostrarDialogoPagamento && dividaSelecionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPagamento = false },
            title = { Text("Pagar Parcela", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                Column {
                    Text("Credor: ${dividaSelecionada?.credor ?: ""}", color = textColor.copy(alpha = 0.7f))
                    val restante = (dividaSelecionada?.valorTotal ?: 0.0) - (dividaSelecionada?.valorPago ?: 0.0)
                    Text("Saldo devedor: R$ ${String.format("%.2f", restante)}", color = textColor.copy(alpha = 0.5f))
                    
                    if ((dividaSelecionada?.totalParcelas ?: 1) > 1) {
                        val valorParcela = (dividaSelecionada?.valorTotal ?: 0.0) / (dividaSelecionada?.totalParcelas ?: 1)
                        Text("Sugestão Parcela: R$ ${String.format("%.2f", valorParcela)}", color = VerdeNeon.copy(alpha = 0.7f), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = valorPagamento,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) valorPagamento = it },
                        label = { Text("Valor a Pagar") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerdeNeon,
                            focusedLabelColor = VerdeNeon,
                            unfocusedTextColor = textColor,
                            focusedTextColor = textColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor = valorPagamento.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (valor > 0 && dividaSelecionada != null) {
                            viewModel.pagarParcela(context, dividaSelecionada!!.id, valor)
                            mostrarDialogoPagamento = false
                            dividaSelecionada = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPagamento = false }) { Text("Cancelar", color = textColor.copy(alpha = 0.5f)) }
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (mostrarDialogoFormDivida) {
        FormDividaDialog(
            dividaExistente = dividaSelecionada,
            onDismiss = { mostrarDialogoFormDivida = false },
            onSave = { credor, valor, parcelas, recorrencia ->
                if (dividaSelecionada != null) {
                    viewModel.editarDivida(context, dividaSelecionada!!.copy(
                        credor = credor,
                        valorTotal = valor,
                        totalParcelas = parcelas,
                        recorrencia = recorrencia
                    ))
                } else {
                    viewModel.adicionarDivida(context, credor, valor, parcelas, recorrencia)
                }
                mostrarDialogoFormDivida = false
            }
        )
    }
}

@Composable
fun CardDivida(
    divida: Divida, 
    onAbrirDialogoPagamento: () -> Unit, 
    onQuitarDivida: (String) -> Unit, 
    onExcluir: (String) -> Unit,
    onEditar: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val progresso = if (divida.valorTotal > 0) (divida.valorPago / divida.valorTotal).toFloat().coerceIn(0f, 1f) else 0f
    val corTotalPago = Color(0xFF34D399)

    PremiumGlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = divida.credor, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.9f))
                    if (divida.totalParcelas > 1) {
                        Text(
                            text = "${divida.parcelasPagas}/${divida.totalParcelas} parcelas • ${divida.recorrencia}",
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.4f)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onExcluir(divida.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                    }
                }
            }
            Text(text = "R$ ${String.format("%.2f", divida.valorTotal)}", color = textColor.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = corTotalPago,
                trackColor = textColor.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Pago: R$ ${String.format("%.2f", divida.valorPago)}", color = corTotalPago, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(text = "${(progresso * 100).toInt()}%", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAbrirDialogoPagamento,
                    enabled = divida.valorPago < divida.valorTotal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.1f), contentColor = textColor)
                ) { Text("Pagar Parcela", fontSize = 11.sp) }
                Button(
                    onClick = { onQuitarDivida(divida.id) },
                    enabled = divida.valorPago < divida.valorTotal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
                ) { Text("Quitar", fontSize = 11.sp) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDividaDialog(
    dividaExistente: Divida?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    var credor by remember { mutableStateOf(dividaExistente?.credor ?: "") }
    var valorTotal by remember { mutableStateOf(dividaExistente?.valorTotal?.toString() ?: "") }
    var parcelas by remember { mutableStateOf(dividaExistente?.totalParcelas?.toString() ?: "1") }
    var recorrencia by remember { mutableStateOf(dividaExistente?.recorrencia ?: "Mês") }
    var expanded by remember { mutableStateOf(false) }
    
    val recorrencias = listOf("Semana", "Quinzena", "Mês", "Ano")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dividaExistente != null) "Editar Dívida" else "Nova Dívida", fontWeight = FontWeight.Bold, color = textColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = credor,
                    onValueChange = { credor = it },
                    label = { Text("Credor / Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VerdeNeon, 
                        focusedLabelColor = VerdeNeon,
                        unfocusedTextColor = textColor,
                        focusedTextColor = textColor
                    )
                )
                
                OutlinedTextField(
                    value = valorTotal,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) valorTotal = it },
                    label = { Text("Valor Total (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VerdeNeon, 
                        focusedLabelColor = VerdeNeon,
                        unfocusedTextColor = textColor,
                        focusedTextColor = textColor
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = parcelas,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) parcelas = it },
                        label = { Text("Parcelas") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerdeNeon, 
                            focusedLabelColor = VerdeNeon,
                            unfocusedTextColor = textColor,
                            focusedTextColor = textColor
                        )
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        OutlinedTextField(
                            value = recorrencia,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frequência") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdeNeon, 
                                focusedLabelColor = VerdeNeon,
                                unfocusedTextColor = textColor,
                                focusedTextColor = textColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            recorrencias.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = item) },
                                    onClick = {
                                        recorrencia = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = valorTotal.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val p = parcelas.toIntOrNull() ?: 1
                    if (credor.isNotBlank() && v > 0) {
                        onSave(credor, v, p, recorrencia)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = textColor.copy(alpha = 0.5f)) }
        },
        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
