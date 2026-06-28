package com.raffastudioproducoes.minharota.ui.screens.hoje

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
import com.raffastudioproducoes.minharota.ui.components.TimeInput
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.util.TextRecognitionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojeScreen(viewModel: HojeViewModel = viewModel()) {
    val context = LocalContext.current
    
    val dataRegistro by viewModel.dataRegistro.collectAsState()
    val ganhoBruto by viewModel.ganhoBruto.collectAsState()
    val ganhoLiquido by viewModel.ganhoLiquido.collectAsState()
    val valorPorHora by viewModel.valorPorHora.collectAsState()
    val horasTrabalhadas by viewModel.horasTrabalhadas.collectAsState()
    val metaDiaria by viewModel.metaDiaria.collectAsState()
    val listaCustos by viewModel.listaCustos.collectAsState()
    val exibirAlertaMei by viewModel.exibirAlertaMei.collectAsState()
    val faturamentoAcumulado by viewModel.faturamentoBrutoAcumulado.collectAsState()
    val isFolga by viewModel.isFolga.collectAsState()
    
    val horaInicio by viewModel.horaInicio.collectAsState()
    val horaFim by viewModel.horaFim.collectAsState()
    val houvePausa by viewModel.houvePausa.collectAsState()
    val horaInicioPausa by viewModel.horaInicioPausa.collectAsState()
    val horaFimPausa by viewModel.horaFimPausa.collectAsState()

    var descCusto by remember { mutableStateOf("") }
    var valorCusto by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var metaDiariaInput by remember { mutableStateOf(if (metaDiaria > 0) metaDiaria.toString() else "") }
    var ganhoBrutoInput by remember { mutableStateOf(if (ganhoBruto > 0) ganhoBruto.toString() else "") }

    var showAccessibilityDialog by remember { mutableStateOf(false) }

    val fundoCarbono = Color(0xFF0C0C0E)
    val verdeEsmeralda = Color(0xFF10B981)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                TextRecognitionHelper.extractValueFromImage(
                    context = context,
                    imageUri = it,
                    onValueFound = { valor ->
                        if (valor > 0) {
                            viewModel.updateGanhoBruto(valor)
                            ganhoBrutoInput = valor.toString()
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

    LaunchedEffect(Unit) {
        viewModel.carregarDadosMei(context)
    }

    Box(modifier = Modifier.fillMaxSize().background(fundoCarbono)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // ALERTA MEI
            if (exibirAlertaMei) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("LIMITE MEI ATINGIDO!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Seu faturamento bruto (R$ ${String.format("%.2f", faturamentoAcumulado)}) está próximo ou acima do teto anual do MEI.",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // GATILHO ACESSIBILIDADE
            item {
                PremiumGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { showAccessibilityDialog = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color(0xFFFACC15))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Assistente de Corrida", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Ative para capturar Uber, 99 e iFood automaticamente.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                HojeSectionCard(
                    title = "DATA DO REGISTRO",
                    subtitle = "Data (padrão: hoje)",
                    icon = Icons.Rounded.CalendarToday
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { showDatePicker = true },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = dataRegistro, color = Color.White)
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = VerdeNeon)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FOLGA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isFolga) VerdeNeon else Color.White.copy(alpha = 0.3f))
                            Switch(
                                checked = isFolga,
                                onCheckedChange = { viewModel.toggleFolga() },
                                colors = SwitchDefaults.colors(checkedThumbColor = VerdeNeon, checkedTrackColor = VerdeNeon.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                HojeSectionCard(
                    title = "HORÁRIOS DE TRABALHO",
                    icon = Icons.Rounded.AccessTime
                ) {
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
                            colors = CheckboxDefaults.colors(checkedColor = verdeEsmeralda)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Horas trabalhadas: $horasTrabalhadas", color = VerdeNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                HojeSectionCard(
                    title = "VALORES DO DIA E CUSTOS",
                    icon = Icons.Rounded.AttachMoney
                ) {
                    OutlinedTextField(
                        value = ganhoBrutoInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                val normalized = input.replace(',', '.')
                                ganhoBrutoInput = normalized
                                viewModel.updateGanhoBruto(normalized.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ganho bruto (R$)") },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.PhotoCamera, contentDescription = "IA", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("IA", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeEsmeralda)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Custos de rua / pessoal", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (listaCustos.isEmpty()) {
                        Text("Nenhum custo adicionado.", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        listaCustos.forEach { custo ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(custo.descricao, color = Color.White, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("R$ ${String.format("%.2f", custo.valor)}", color = Color.White, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp).clickable { viewModel.removerCusto(custo.id) }.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = descCusto, onValueChange = { descCusto = it }, modifier = Modifier.weight(1.5f), placeholder = { Text("Gasolina...", fontSize = 12.sp) }, shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = valorCusto, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) valorCusto = it }, modifier = Modifier.weight(1f), placeholder = { Text("Valor", fontSize = 12.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp))
                        IconButton(onClick = {
                            val v = valorCusto.replace(',', '.').toDoubleOrNull() ?: 0.0
                            if (v > 0 && descCusto.isNotBlank()) {
                                viewModel.adicionarCusto(descCusto, v)
                                descCusto = ""; valorCusto = ""
                            }
                        }, modifier = Modifier.size(48.dp).background(verdeEsmeralda, RoundedCornerShape(12.dp))) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                HojeSectionCard(
                    title = "RESUMO DE GANHOS",
                    icon = Icons.Rounded.PieChart
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("GANHO LÍQUIDO", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("R$ ${String.format("%.2f", ganhoLiquido)}", color = VerdeNeon, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("VALOR / HORA", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("R$ ${String.format("%.2f", valorPorHora)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("META DIÁRIA", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { if (metaDiaria > 0) (ganhoBruto / metaDiaria).toFloat().coerceIn(0f, 1f) else 0f },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = VerdeNeon,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${if (metaDiaria > 0) (ganhoBruto / metaDiaria * 100).toInt() else 0}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("R$ ${String.format("%.2f", ganhoBruto)} de R$ ${String.format("%.2f", metaDiaria)}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
        }

        // Botão Salvar Turno (Pílula)
        Button(
            onClick = { viewModel.salvarTurno(context, onSuccess = {
                Toast.makeText(context, "Turno salvo com sucesso!", Toast.LENGTH_SHORT).show()
            }) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = verdeEsmeralda)
        ) {
            Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SALVAR TURNO DE HOJE", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        // Diálogo de Acessibilidade Glassmorphic v1.9.2
        if (showAccessibilityDialog) {
            AlertDialog(
                onDismissRequest = { showAccessibilityDialog = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                PremiumGlassCard(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.AccessibilityNew, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Ativar Monitoramento",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Para capturar suas corridas automaticamente, você precisa ativar o 'Assistente de Corrida MinhaRota' nas configurações de Acessibilidade do Android.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                showAccessibilityDialog = false
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon)
                        ) {
                            Text("IR PARA CONFIGURAÇÕES", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showAccessibilityDialog = false }) {
                            Text("AGORA NÃO", color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.alterarDataRegistro(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        }
                        showDatePicker = false
                    }) { Text("OK", color = VerdeNeon) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR", color = Color.White.copy(alpha = 0.5f)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun HojeSectionCard(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        }
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            if (subtitle != null) {
                Text(text = subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
            }
            content()
        }
    }
}
