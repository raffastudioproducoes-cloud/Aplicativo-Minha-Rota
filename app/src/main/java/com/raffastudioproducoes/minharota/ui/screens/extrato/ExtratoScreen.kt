package com.raffastudioproducoes.minharota.ui.screens.extrato

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.domain.model.Movimentacao
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun ExtratoScreen(viewModel: ExtratoViewModel = viewModel()) {
    val context = LocalContext.current
    val movimentacoes by viewModel.movimentacoes.collectAsState()
    val filtroSelecionado by viewModel.filtroSelecionado.collectAsState()

    // Cores específicas do Extrato v1.7.0
    val corEntradas = Color(0xFF34D399) // Verde Menta Neon
    val corSaidas = Color(0xFFF87171)   // Coral Suave Premium
    val corSaldo = Color(0xFF10B981)    // Verde Esmeralda Vibrante

    LaunchedEffect(Unit) {
        viewModel.carregarMovimentacoes(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoDark)) {
        Text(
            text = "Meu Extrato",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        // Filtros em pílula Premium
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filtros = listOf("7 dias", "15 dias", "Este mês", "Todos")
            filtros.forEach { filtro ->
                val isSelected = filtroSelecionado == filtro
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.aplicarFiltro(filtro) },
                    label = { Text(filtro) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        selectedContainerColor = VerdeNeon,
                        labelColor = Color.White.copy(alpha = 0.7f),
                        selectedLabelColor = Color.Black
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.White.copy(alpha = 0.1f),
                        selectedBorderColor = Color.Transparent,
                        borderWidth = 1.dp
                    )
                )
            }
        }

        // Seção de Cards de Saldo Independentes (Refatoração v1.7.0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Entradas
            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Entradas", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("R$ 335,00", color = corEntradas, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Card 2: Saídas
            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Saídas", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("R$ 67,00", color = corSaidas, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Card 3: Saldo Total
            PremiumGlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Saldo", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("R$ 268,00", color = corSaldo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(movimentacoes) { mov ->
                CardMovimentacao(mov)
            }
        }
    }
}

@Composable
fun CardMovimentacao(mov: Movimentacao) {
    val cor = if (mov.tipo == "ENTRADA") Color(0xFF34D399) else Color(0xFFF87171)
    
    PremiumGlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = cor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (mov.tipo == "ENTRADA") Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = cor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(mov.descricao, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text(mov.data, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            
            Text(
                text = "${if (mov.tipo == "ENTRADA") "+" else "-"} R$ ${String.format("%.2f", mov.valor)}",
                fontWeight = FontWeight.Bold,
                color = cor,
                fontSize = 14.sp
            )
        }
    }
}
