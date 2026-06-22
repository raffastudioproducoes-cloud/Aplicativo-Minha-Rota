package com.raffastudioproducoes.minharota.ui.screens.hoje

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.components.TimeInput
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada
import com.raffastudioproducoes.minharota.util.TextRecognitionHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HojeScreen(viewModel: HojeViewModel = viewModel()) {
    val context = LocalContext.current
    
    val ganhoBruto by viewModel.ganhoBruto.collectAsState()
    val custoRua by viewModel.custoRua.collectAsState()
    val ganhoLiquido by viewModel.ganhoLiquido.collectAsState()
    val valorPorHora by viewModel.valorPorHora.collectAsState()
    val horasTrabalhadas by viewModel.horasTrabalhadas.collectAsState()
    val isRidingMode by viewModel.isRidingMode.collectAsState()
    val ganhosRapidos by viewModel.ganhosRapidos.collectAsState()
    val listaCustos by viewModel.listaCustos.collectAsState()
    val metaDiaria by viewModel.metaDiaria.collectAsState()
    
    val horaInicio by viewModel.horaInicio.collectAsState()
    val horaFim by viewModel.horaFim.collectAsState()
    val houvePausa by viewModel.houvePausa.collectAsState()
    val horaInicioPausa by viewModel.horaInicioPausa.collectAsState()
    val horaFimPausa by viewModel.horaFimPausa.collectAsState()

    var mostrarModalRapido by remember { mutableStateOf(false) }
    var valorPreenchidoOcr by remember { mutableStateOf<Double?>(null) }
    
    // Estados locais para inputs de custos
    var descCusto by remember { mutableStateOf("") }
    var valorCusto by remember { mutableStateOf("") }
    
    // Estado local para Meta Diária (evita formatação agressiva durante a digitação)
    var metaDiariaInput by remember { mutableStateOf(if (metaDiaria > 0) metaDiaria.toString() else "") }
    
    // Sincronizar metaDiariaInput quando metaDiaria mudar no ViewModel (ex: ao limpar campos)
    LaunchedEffect(metaDiaria) {
        if (metaDiaria == 0.0) {
            metaDiariaInput = ""
        } else if (metaDiariaInput.toDoubleOrNull() != metaDiaria) {
            metaDiariaInput = metaDiaria.toString()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                TextRecognitionHelper.extractValueFromImage(
                    context = context,
                    imageUri = it,
                    onValueFound = { valor ->
                        if (valor > 0) {
                            valorPreenchidoOcr = valor
                            mostrarModalRapido = true
                        } else {
                            Toast.makeText(context, "Não foi possível identificar o valor no print.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = {
                        Toast.makeText(context, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (isRidingMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "R$ ${String.format("%.2f", ganhoLiquido)}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeEntrada
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            valorPreenchidoOcr = null
                            mostrarModalRapido = true 
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeEntrada)
                    ) {
                        Text("Ganho Rápido", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.toggleRidingMode() }) {
                        Text("Sair do Modo Riding", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                item {
                    // 1. DASHBOARD CORE (PRESERVADO)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.04f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ganho Acumulado do Dia",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "R$ ${String.format("%.2f", ganhoLiquido)}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = VerdeEntrada
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MiniInfo(label = "Horas", value = horasTrabalhadas)
                                VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.1f))
                                MiniInfo(label = "R$/Hora", value = "R$ ${String.format("%.2f", valorPorHora)}")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // BOTÃO MODO RIDING
                    Button(
                        onClick = { viewModel.toggleRidingMode() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Rounded.TwoWheeler, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entrar no Modo Riding", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BLOCO 1: DATA DO REGISTRO
                    SectionCard(title = "Data do Registro") {
                        val hoje = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        OutlinedTextField(
                            value = hoje,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Data") },
                            leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BLOCO 2: HORÁRIOS DE TRABALHO
                    SectionCard(title = "Horários de Trabalho") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                TimeInput(label = "Início", selectedTime = horaInicio, onTimeSelected = { viewModel.updateHoraInicio(it) })
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TimeInput(label = "Término", selectedTime = horaFim, onTimeSelected = { viewModel.updateHoraFim(it) })
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = houvePausa,
                                onCheckedChange = { viewModel.updateHouvePausa(it) },
                                colors = CheckboxDefaults.colors(checkedColor = VerdeEntrada)
                            )
                            Text("Houve intervalo / pausa?", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }

                        if (houvePausa) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    TimeInput(label = "Início Pausa", selectedTime = horaInicioPausa, onTimeSelected = { viewModel.updateHoraInicioPausa(it) })
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    TimeInput(label = "Fim Pausa", selectedTime = horaFimPausa, onTimeSelected = { viewModel.updateHoraFimPausa(it) })
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total trabalhado: $horasTrabalhadas",
                            color = VerdeEntrada,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BLOCO 3: VALORES DO DIA
                    SectionCard(title = "Valores do Dia") {
                        OutlinedTextField(
                            value = if (ganhoBruto > 0) String.format("%.2f", ganhoBruto) else "",
                            onValueChange = { val v = it.toDoubleOrNull() ?: 0.0; viewModel.updateGanhoBruto(v) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Ganho Bruto (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) {
                                    BadgedBox(badge = { Badge(containerColor = Color(0xFFF59E0B)) { Text("IA", color = Color.Black, fontWeight = FontWeight.Bold) } }) {
                                        Icon(Icons.Rounded.PhotoCamera, contentDescription = "OCR IA", tint = Color.White)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Custos de rua / pessoal", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = descCusto,
                                onValueChange = { descCusto = it },
                                modifier = Modifier.weight(1.5f),
                                label = { Text("Descrição") },
                                placeholder = { Text("Ex: Almoço") }
                            )
                            OutlinedTextField(
                                value = valorCusto,
                                onValueChange = { valorCusto = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("R$") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            IconButton(
                                onClick = { 
                                    val v = valorCusto.toDoubleOrNull() ?: 0.0
                                    if (v > 0 && descCusto.isNotBlank()) {
                                        viewModel.adicionarCusto(descCusto, v)
                                        descCusto = ""
                                        valorCusto = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterVertically).background(VerdeEntrada, CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Adicionar", tint = Color.Black)
                            }
                        }
                    }
                }

                // LISTA DINÂMICA DE CUSTOS
                items(listaCustos) { custo ->
                    CustoRow(custo = custo, onRemove = { viewModel.removerCusto(custo.id) })
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // PAINEL DE RESUMO E PROGRESSO DA META
                    SectionCard(title = "Resumo e Meta") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ResumoItem(label = "Líquido", value = "R$ ${String.format("%.2f", ganhoLiquido)}", color = if (ganhoLiquido >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                            ResumoItem(label = "Por hora", value = "R$ ${String.format("%.2f", valorPorHora)}", color = Color.White)
                            ResumoItem(label = "Meta", value = "R$ ${String.format("%.2f", metaDiaria)}", color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = metaDiariaInput,
                            onValueChange = { 
                                // Permite apenas números e um ponto decimal
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    metaDiariaInput = it
                                    val v = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateMetaDiaria(v)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Meta diária manual (R$)") },
                            placeholder = { Text("Ex: 150.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        val progresso = if (metaDiaria > 0) (ganhoLiquido / metaDiaria).coerceIn(0.0, 1.0).toFloat() else 0f
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = VerdeEntrada,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Text(
                            text = "${(progresso * 100).toInt()}% da meta atingida",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // BOTÃO DE SALVAMENTO
                    Button(
                        onClick = { 
                            viewModel.salvarTurno(context) {
                                Toast.makeText(context, "Dia salvo e distribuído com sucesso!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar dia e distribuir nas caixinhas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        if (mostrarModalRapido) {
            com.raffastudioproducoes.minharota.ui.components.ModalRegistroRapido(
                valorInicial = valorPreenchidoOcr ?: 0.0,
                onDismiss = { 
                    mostrarModalRapido = false
                    valorPreenchidoOcr = null
                },
                onSave = { valor ->
                    viewModel.registrarGanhoRapido(valor)
                    mostrarModalRapido = false
                    valorPreenchidoOcr = null
                }
            )
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.03f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun CustoRow(custo: HojeViewModel.CustoItem, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.02f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = custo.descricao, color = Color.White, fontWeight = FontWeight.Medium)
                Text(text = "R$ ${String.format("%.2f", custo.valor)}", color = Color(0xFFEF4444), fontSize = 12.sp)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Delete, contentDescription = "Remover", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ResumoItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun MiniInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
