package com.raffastudioproducoes.minharota.ui.screens.dividas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.domain.model.Divida
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun DividasScreen(viewModel: DividasViewModel = viewModel()) {
    val context = LocalContext.current
    val dividas by viewModel.dividas.collectAsState()
    var mostrarDialogoPagamento by remember { mutableStateOf(false) }
    var dividaSelecionada by remember { mutableStateOf<Divida?>(null) }
    var valorPagamento by remember { mutableStateOf("") }

    // Cores específicas de Dívidas v1.7.0
    val corEmAberto = Color(0xFFFB7185) // Coral/Alerta
    val corTotalPago = Color(0xFF34D399) // Verde Conquistado

    LaunchedEffect(Unit) {
        viewModel.carregarDividas(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoDark)) {
        Text(
            text = "Minhas Dívidas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        // Seção de Totalizadores Independentes (Refatoração v1.7.0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Em aberto
            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Em aberto", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R$ ${String.format("%.2f", dividas.sumOf { it.valorTotal - it.valorPago })}",
                        color = corEmAberto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Card 2: Total Pago
            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Total Pago", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
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
                    onQuitarDivida = { id -> viewModel.quitarDivida(context, id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Implementar adicionar dívida */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Dívida", color = Color.White)
                }
            }
        }
    }

    // AlertDialog para Pagamento Parcial
    if (mostrarDialogoPagamento && dividaSelecionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPagamento = false },
            title = { Text("Pagar Parcela", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Credor: ${dividaSelecionada?.credor ?: ""}", color = Color.White.copy(alpha = 0.7f))
                    Text("Saldo devedor: R$ ${String.format("%.2f", dividaSelecionada?.let { it.valorTotal - it.valorPago } ?: 0.0)}", color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = valorPagamento,
                        onValueChange = { valorPagamento = it },
                        label = { Text("Valor a Pagar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerdeNeon,
                            focusedLabelColor = VerdeNeon
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor = valorPagamento.toDoubleOrNull() ?: 0.0
                        if (valor > 0 && dividaSelecionada != null) {
                            viewModel.pagarParcela(context, dividaSelecionada!!.id, valor)
                            mostrarDialogoPagamento = false
                            dividaSelecionada = null
                            valorPagamento = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon)
                ) {
                    Text("Confirmar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPagamento = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CardDivida(divida: Divida, onAbrirDialogoPagamento: () -> Unit, onQuitarDivida: (String) -> Unit) {
    val progresso = (divida.valorPago / divida.valorTotal).toFloat()
    val corTotalPago = Color(0xFF34D399)

    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = divida.credor, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                Text(
                    text = "R$ ${String.format("%.2f", divida.valorTotal)}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = corTotalPago,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pago: R$ ${String.format("%.2f", divida.valorPago)}",
                    color = corTotalPago,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progresso * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAbrirDialogoPagamento,
                    enabled = divida.valorPago < divida.valorTotal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Text("Parcela", fontSize = 12.sp)
                }
                Button(
                    onClick = { onQuitarDivida(divida.id) },
                    enabled = divida.valorPago < divida.valorTotal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon)
                ) {
                    Text("Quitar", color = Color.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
