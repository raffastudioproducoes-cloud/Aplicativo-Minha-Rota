package com.raffastudioproducoes.minharota.ui.screens.garagem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaragemScreen(viewModel: GaragemViewModel = viewModel()) {
    val context = LocalContext.current
    val kmAtual by viewModel.kmAtual.collectAsState()
    val manutencoes by viewModel.manutencoes.collectAsState()
    val mediaKmL by viewModel.mediaResult.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var kmInput by remember { mutableStateOf(if (kmAtual > 0) kmAtual.toString() else "") }
    
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.carregarDados(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. CARD QUILOMETRAGEM ATUAL
            item {
                PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "QUILOMETRAGEM ATUAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = kmInput,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) kmInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ex: 15400", color = Color.White.copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdeNeon)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.atualizarKmAtual(context, kmInput.toIntOrNull() ?: 0) },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Atualizar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. CARD MÉDIA DE CONSUMO
            item {
                PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalGasStation, contentDescription = null, tint = VerdeNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EFICIÊNCIA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.1f", mediaKmL)} Km/L",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. TÍTULO MANUTENÇÕES E BOTÃO ADICIONAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MANUTENÇÕES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    TextButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar", color = VerdeNeon)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 4. LISTA DE MANUTENÇÕES
            if (manutencoes.isEmpty()) {
                item {
                    Text(
                        "Nenhuma manutenção registrada.",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(manutencoes) { manutencao ->
                    val kmRestante = (manutencao.ultimoServicoKm + manutencao.intervaloKm) - kmAtual
                    val isCritico = kmRestante <= 0
                    
                    ManutencaoCard(
                        manutencao = manutencao,
                        kmRestante = kmRestante,
                        isCritico = isCritico,
                        onDelete = { viewModel.excluirManutencao(context, manutencao.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1A1A1C)
        ) {
            NewManutencaoForm(
                onSave = { nome, intervalo, ultimo, icone ->
                    viewModel.adicionarManutencao(context, nome, intervalo, ultimo, icone)
                    showBottomSheet = false
                },
                onCancel = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun ManutencaoCard(manutencao: Manutencao, kmRestante: Int, isCritico: Boolean, onDelete: () -> Unit) {
    PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isCritico) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(manutencao.icone) {
                        "oil" -> Icons.Rounded.Opacity
                        "settings" -> Icons.Rounded.Settings
                        else -> Icons.Rounded.Build
                    },
                    contentDescription = null,
                    tint = if (isCritico) Color.Red else Color.White.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(manutencao.nome, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = if (isCritico) "REVISÃO CRÍTICA!" else "Próxima em $kmRestante km",
                    color = if (isCritico) Color.Red else Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val progresso = if (manutencao.intervaloKm > 0) {
            val rodado = kmRestante.coerceIn(0, manutencao.intervaloKm)
            (rodado.toFloat() / manutencao.intervaloKm.toFloat())
        } else 0f
        
        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = if (isCritico) Color.Red else VerdeNeon,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun NewManutencaoForm(onSave: (String, Int, Int, String) -> Unit, onCancel: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var intervalo by remember { mutableStateOf("") }
    var ultimo by remember { mutableStateOf("") }
    var iconeSelecionado by remember { mutableStateOf("build") }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("Nova Manutenção", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome (Ex: Troca de óleo)") },
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = intervalo,
                onValueChange = { if (it.all { c -> c.isDigit() }) intervalo = it },
                modifier = Modifier.weight(1f),
                label = { Text("Intervalo (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = ultimo,
                onValueChange = { if (it.all { c -> c.isDigit() }) ultimo = it },
                modifier = Modifier.weight(1f),
                label = { Text("Último serviço (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Ícone", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("build", "oil", "settings").forEach { icon ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (iconeSelecionado == icon) VerdeNeon else Color.White.copy(alpha = 0.05f))
                        .clickable { iconeSelecionado = icon },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(icon) {
                            "oil" -> Icons.Rounded.Opacity
                            "settings" -> Icons.Rounded.Settings
                            else -> Icons.Rounded.Build
                        },
                        contentDescription = null,
                        tint = if (iconeSelecionado == icon) Color.Black else Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.5f))
            }
            Button(
                onClick = { 
                    if (nome.isNotBlank()) {
                        onSave(nome, intervalo.toIntOrNull() ?: 0, ultimo.toIntOrNull() ?: 0, iconeSelecionado)
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
