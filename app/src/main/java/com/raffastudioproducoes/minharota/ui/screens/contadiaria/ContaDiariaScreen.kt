package com.raffastudioproducoes.minharota.ui.screens.contadiaria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.domain.model.ContaDiaria
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard

@Composable
fun ContaDiariaScreen(viewModel: ContaDiariaViewModel = viewModel()) {
    val contas by viewModel.contasDiarias.collectAsState()
    val metaDiaria by viewModel.metaDiaria.collectAsState()

    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var frequenciaSelecionada by remember { mutableStateOf("Mensal") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "⭐ Conta Diária Inteligente",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Card Vidro Fosco - Meta Diária
        PremiumGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Valor Necessário por Dia",
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0B0),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "R$ ${"%.2f".format(metaDiaria)}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Formulário de Entrada
        Text(
            text = "Adicionar Nova Conta",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição", color = Color(0xFFB0B0B0)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Valor (R$)", color = Color(0xFFB0B0B0)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Seletor de Frequência
        Text(
            text = "Frequência",
            fontSize = 12.sp,
            color = Color(0xFFB0B0B0),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Diario", "Semanal", "Quinzenal", "Mensal").forEach { freq ->
                Button(
                    onClick = { frequenciaSelecionada = freq },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (frequenciaSelecionada == freq) Color(0xFF2ECC71) else Color(0xFF3A3A3C)
                    )
                ) {
                    Text(
                        text = freq,
                        fontSize = 11.sp,
                        color = if (frequenciaSelecionada == freq) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Botão Adicionar
        Button(
            onClick = {
                if (descricao.isNotEmpty() && valor.isNotEmpty()) {
                    viewModel.adicionarConta(descricao, valor.toDouble(), frequenciaSelecionada)
                    descricao = ""
                    valor = ""
                    frequenciaSelecionada = "Mensal"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2ECC71)
            )
        ) {
            Text("Adicionar Conta", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        // Lista de Contas
        Text(
            text = "Minhas Contas (${contas.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contas) { conta ->
                ContaDiariaItem(
                    conta = conta,
                    onDelete = { viewModel.removerConta(conta.id) }
                )
            }
        }
    }
}

@Composable
fun ContaDiariaItem(conta: ContaDiaria, onDelete: () -> Unit) {
    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = conta.descricao,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${conta.frequencia} • R$ ${"%.2f".format(conta.valor)}",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Deletar",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
