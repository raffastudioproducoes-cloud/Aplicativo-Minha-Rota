package com.raffastudioproducoes.minharota.ui.screens.garagem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.raffastudioproducoes.minharota.ui.components.AiInsightCard
import com.raffastudioproducoes.minharota.domain.subscription.SubscriptionPurchasePolicy
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.viewmodel.GeminiAiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaragemScreen(
    viewModel: GaragemViewModel = viewModel(),
    geminiViewModel: GeminiAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val kmAtual by viewModel.kmAtual.collectAsState()
    val kmTotal by viewModel.kmTotalAcumulado.collectAsState()
    val manutencoes by viewModel.manutencoes.collectAsState()
    val mediaKmL by viewModel.mediaResult.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    var showBottomSheet by remember { mutableStateOf(false) }
    var manutencaoParaEditar by remember { mutableStateOf<Manutencao?>(null) }
    var kmInput by remember { mutableStateOf("") }
    
    val sheetState = rememberModalBottomSheetState()

    // Coletar estados do Gemini AI
    val garagemInsight by geminiViewModel.garagemInsight.collectAsState()
    val garagemIsLoading by geminiViewModel.garagemIsLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarDados(context)
    }

    // Disparar insight ao carregar (somente PRO — o ViewModel verifica internamente)
    LaunchedEffect(kmTotal, manutencoes) {
        val proximasManutencoes = manutencoes
            .filter { !it.concluida }
            .joinToString("; ") { "${it.nome} (a cada ${it.intervaloKm} km)" }
            .ifBlank { "Nenhuma manutenção pendente" }
        geminiViewModel.gerarInsightGaragem(
            context = context,
            kmTotal = kmTotal,
            proximasManutencoes = proximasManutencoes
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI INSIGHT CARD (PRO) — topo absoluto da tela
            item {
                AiInsightCard(
                    isPro = SubscriptionPurchasePolicy.hasVerifiedPaidEntitlement(),
                    isLoading = garagemIsLoading,
                    insight = garagemInsight,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 1. CARDS DE QUILOMETRAGEM (Total e Atual)
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // KM TOTAL ACUMULADO
                    PremiumGlassCard(modifier = Modifier.weight(1f)) {
                        Text("KM TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.5f))
                        Text("${kmTotal} km", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = VerdeNeon)
                    }
                    // MÉDIA EFICIÊNCIA
                    PremiumGlassCard(modifier = Modifier.weight(1f)) {
                        Text("EFICIÊNCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.5f))
                        Text("${String.format("%.1f", mediaKmL)} km/L", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // ATUALIZAÇÃO DO HODÔMETRO
                PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ATUALIZAR HODÔMETRO (ATUAL: $kmAtual km)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = kmInput,
                            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) kmInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Novo KM...", color = textColor.copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdeNeon,
                                unfocusedTextColor = textColor,
                                focusedTextColor = textColor
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { 
                                val novo = kmInput.toIntOrNull() ?: 0
                                if (novo > 0) {
                                    viewModel.atualizarKmAtual(context, novo)
                                    kmInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Atualizar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. TÍTULO MANUTENÇÕES E BOTÃO ADICIONAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MANUTENÇÕES AGENDADAS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                    TextButton(onClick = { 
                        manutencaoParaEditar = null
                        showBottomSheet = true 
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar", color = VerdeNeon)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. LISTA DE MANUTENÇÕES
            if (manutencoes.isEmpty()) {
                item {
                    Text(
                        "Nenhuma manutenção registrada.",
                        color = textColor.copy(alpha = 0.3f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(manutencoes) { manutencao ->
                    val kmRestante = (manutencao.ultimoServicoKm + manutencao.intervaloKm) - kmAtual
                    val isCritico = kmRestante <= 0 && !manutencao.concluida
                    
                    ManutencaoCard(
                        manutencao = manutencao,
                        kmRestante = kmRestante,
                        isCritico = isCritico,
                        onDelete = { viewModel.excluirManutencao(context, manutencao.id) },
                        onEdit = { 
                            manutencaoParaEditar = manutencao
                            showBottomSheet = true
                        },
                        onConcluir = { viewModel.concluirManutencao(context, manutencao.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF1A1A1C) else Color.White
        ) {
            ManutencaoForm(
                manutencaoExistente = manutencaoParaEditar,
                onSave = { nome, intervalo, ultimo, icone ->
                    if (manutencaoParaEditar != null) {
                        viewModel.editarManutencao(context, manutencaoParaEditar!!.copy(
                            nome = nome, intervaloKm = intervalo, ultimoServicoKm = ultimo, icone = icone
                        ))
                    } else {
                        viewModel.adicionarManutencao(context, nome, intervalo, ultimo, icone)
                    }
                    showBottomSheet = false
                },
                onCancel = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun ManutencaoCard(
    manutencao: Manutencao, 
    kmRestante: Int, 
    isCritico: Boolean, 
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onConcluir: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(
                    if (manutencao.concluida) Color(0xFF10B981).copy(alpha = 0.1f)
                    else if (isCritico) Color.Red.copy(alpha = 0.2f) 
                    else textColor.copy(alpha = 0.05f)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(manutencao.icone) {
                        "oil" -> Icons.Rounded.Opacity
                        "settings" -> Icons.Rounded.Settings
                        else -> Icons.Rounded.Build
                    },
                    contentDescription = null,
                    tint = if (manutencao.concluida) Color(0xFF10B981) else if (isCritico) Color.Red else textColor.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(manutencao.nome, fontWeight = FontWeight.Bold, color = if (manutencao.concluida) textColor.copy(alpha = 0.6f) else textColor)
                Text(
                    text = if (manutencao.concluida) "Concluída em ${manutencao.dataConclusao}" 
                           else if (isCritico) "REVISÃO CRÍTICA!" 
                           else "Próxima em $kmRestante km",
                    color = if (manutencao.concluida) Color(0xFF10B981) else if (isCritico) Color.Red else textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            
            Row {
                if (!manutencao.concluida) {
                    IconButton(onClick = onConcluir) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Concluir", tint = VerdeNeon.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Editar", tint = textColor.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Excluir", tint = Color.Red.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                }
            }
        }
        
        if (!manutencao.concluida) {
            Spacer(modifier = Modifier.height(12.dp))
            val progresso = if (manutencao.intervaloKm > 0) {
                val rodado = kmRestante.coerceIn(0, manutencao.intervaloKm)
                (rodado.toFloat() / manutencao.intervaloKm.toFloat())
            } else 0f
            
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = if (isCritico) Color.Red else VerdeNeon,
                trackColor = textColor.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun ManutencaoForm(manutencaoExistente: Manutencao?, onSave: (String, Int, Int, String) -> Unit, onCancel: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    var nome by remember { mutableStateOf(manutencaoExistente?.nome ?: "") }
    var intervalo by remember { mutableStateOf(manutencaoExistente?.intervaloKm?.toString() ?: "") }
    var ultimo by remember { mutableStateOf(manutencaoExistente?.ultimoServicoKm?.toString() ?: "") }
    var iconeSelecionado by remember { mutableStateOf(manutencaoExistente?.icone ?: "build") }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text(if (manutencaoExistente != null) "Editar Manutenção" else "Nova Manutenção", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome (Ex: Troca de óleo)") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeNeon,
                unfocusedTextColor = textColor,
                focusedTextColor = textColor
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = intervalo,
                onValueChange = { if (it.all { c -> c.isDigit() }) intervalo = it },
                modifier = Modifier.weight(1f),
                label = { Text("Intervalo (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                )
            )
            OutlinedTextField(
                value = ultimo,
                onValueChange = { if (it.all { c -> c.isDigit() }) ultimo = it },
                modifier = Modifier.weight(1f),
                label = { Text("Último serviço (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Ícone", fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("build", "oil", "settings").forEach { icon ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (iconeSelecionado == icon) VerdeNeon else textColor.copy(alpha = 0.05f))
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
                        tint = if (iconeSelecionado == icon) Color.Black else textColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar", color = textColor.copy(alpha = 0.5f))
            }
            Button(
                onClick = { 
                    val inter = intervalo.toIntOrNull() ?: 0
                    val ult = ultimo.toIntOrNull() ?: 0
                    if (nome.isNotBlank() && inter > 0) {
                        onSave(nome, inter, ult, iconeSelecionado)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
