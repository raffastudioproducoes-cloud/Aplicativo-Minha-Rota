package com.raffastudioproducoes.minharota.ui.screens.hoje

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.components.AiInsightCard
import com.raffastudioproducoes.minharota.ui.components.CheckoutModal
import com.raffastudioproducoes.minharota.domain.subscription.SubscriptionPurchasePolicy
import com.raffastudioproducoes.minharota.ui.components.HojeSectionCard
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.components.RenovacaoAlertCard
import com.raffastudioproducoes.minharota.ui.components.TimeInput
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.viewmodel.GeminiAiViewModel
import com.raffastudioproducoes.minharota.util.TextRecognitionHelper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojeScreen(
    viewModel: HojeViewModel = viewModel(),
    geminiViewModel: GeminiAiViewModel = viewModel()
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
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
    
    var showMetaDialog by remember { mutableStateOf(false) }
    var metaInputDialog by remember { mutableStateOf("") }
    var ganhoBrutoInput by remember { mutableStateOf(if (ganhoBruto > 0) ganhoBruto.toString() else "") }
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
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
                    onError = { Toast.makeText(context, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    )

    val hojeInsight by geminiViewModel.hojeInsight.collectAsState()
    val hojeIsLoading by geminiViewModel.hojeIsLoading.collectAsState()
    val exibirAlertaRenovacao by viewModel.exibirAlertaRenovacao.collectAsState()
    val diasParaVencer by viewModel.diasParaVencer.collectAsState()
    val nomePlanoAtivo by viewModel.nomePlanoAtivo.collectAsState()
    var mostrarCheckoutRenovacao by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.carregarDadosMei(context) }
    LaunchedEffect(ganhoBruto, horasTrabalhadas, ganhoLiquido) {
        if (ganhoBruto > 0 || ganhoLiquido > 0) {
            geminiViewModel.gerarInsightHoje(context, ganhoBruto, horasTrabalhadas, ganhoLiquido)
        }
    }

    if (mostrarCheckoutRenovacao) {
        CheckoutModal(
            nomePlano = nomePlanoAtivo.ifBlank { "Premium" },
            onDismiss = { mostrarCheckoutRenovacao = false }
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // AQUI ESTÁ O SEGREDO DO ESPAÇAMENTO AUTOMÁTICO
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            if (exibirAlertaRenovacao && diasParaVencer >= 0) {
                item { RenovacaoAlertCard(nomePlano = nomePlanoAtivo, diasRestantes = diasParaVencer, onRenovar = { mostrarCheckoutRenovacao = true }) }
            }

            item {
                AiInsightCard(
                    isPro = SubscriptionPurchasePolicy.hasVerifiedPaidEntitlement(),
                    isLoading = hojeIsLoading,
                    insight = hojeInsight
                )
            }

            if (exibirAlertaMei) {
                item {
                    PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("LIMITE MEI ATINGIDO!", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Seu faturamento bruto está acima do teto.", color = textColor.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                PremiumGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccessibilityDialog = true }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color(0xFFFACC15))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Assistente de Corrida", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Ative para capturar Uber, 99 e iFood automaticamente.", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                HojeSectionCard(
                    title = "DATA DO REGISTRO ",
                    subtitle = "Data (padrão: hoje)",
                    icon = Icons.Rounded.CalendarToday
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { showDatePicker = true },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = dataRegistro, color = textColor)
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = VerdeNeon)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FOLGA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isFolga) VerdeNeon else textColor.copy(alpha = 0.3f))
                            Switch(
                                checked = isFolga,
                                onCheckedChange = { viewModel.toggleFolga() },
                                colors = SwitchDefaults.colors(checkedThumbColor = VerdeNeon, checkedTrackColor = VerdeNeon.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            item {
                HojeSectionCard(title = "HORÁRIOS DE TRABALHO", icon = Icons.Rounded.AccessTime) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) { TimeInput(label = "Início", selectedTime = horaInicio, onTimeSelected = { viewModel.updateHoraInicio(it) }) }
                        Box(modifier = Modifier.weight(1f)) { TimeInput(label = "Término", selectedTime = horaFim, onTimeSelected = { viewModel.updateHoraFim(it) }) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                        Checkbox(checked = houvePausa, onCheckedChange = { viewModel.updateHouvePausa(it) }, colors = CheckboxDefaults.colors(checkedColor = verdeEsmeralda))
                        Text("Houve intervalo / pausa?", color = textColor.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    if (houvePausa) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { TimeInput(label = "Início Pausa", selectedTime = horaInicioPausa, onTimeSelected = { viewModel.updateHoraInicioPausa(it) }) }
                            Box(modifier = Modifier.weight(1f)) { TimeInput(label = "Fim Pausa", selectedTime = horaFimPausa, onTimeSelected = { viewModel.updateHoraFimPausa(it) }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Horas trabalhadas: $horasTrabalhadas", color = VerdeNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            item {
                HojeSectionCard(title = "VALORES DO DIA E CUSTOS", icon = Icons.Rounded.AttachMoney) {
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
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                    .background(
                                        if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.PhotoCamera, contentDescription = "IA", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("IA", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeEsmeralda, unfocusedTextColor = textColor, focusedTextColor = textColor)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Custos de rua / pessoal", color = textColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (listaCustos.isEmpty()) {
                        Text("Nenhum custo adicionado.", color = textColor.copy(alpha = 0.3f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        listaCustos.forEach { custo ->
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(custo.descricao, color = textColor, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("R$ ${String.format("%.2f", custo.valor)}", color = textColor, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier
                                        .size(18.dp)
                                        .clickable { viewModel.removerCusto(custo.id) }
                                        .padding(start = 8.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = descCusto, onValueChange = { descCusto = it }, modifier = Modifier.weight(1.5f), label = { Text("O que gastou?", fontSize = 12.sp) }, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor))
                        OutlinedTextField(value = valorCusto, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) valorCusto = it.replace(',', '.') }, modifier = Modifier.weight(1f), label = { Text("Valor", fontSize = 12.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = textColor, focusedTextColor = textColor))
                        IconButton(onClick = { if (descCusto.isNotEmpty() && valorCusto.isNotEmpty()) { viewModel.adicionarCusto(descCusto, valorCusto.toDoubleOrNull() ?: 0.0); descCusto = ""; valorCusto = "" } }, modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .background(verdeEsmeralda, CircleShape)) { Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.Black) }
                    }
                }
            }

            item {
                PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text("GANHO LÍQUIDO ESTIMADO", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                        Text(text = "R$ ${String.format("%.2f", ganhoLiquido)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = VerdeNeon)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "R$ ${String.format("%.2f", valorPorHora)} / hora", color = textColor.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("META DIÁRIA", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                                Text(text = "R$ ${String.format("%.2f", metaDiaria)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                            }
                            IconButton(onClick = { metaInputDialog = metaDiaria.toString(); showMetaDialog = true }) {
                                Icon(Icons.Rounded.Edit, contentDescription = null, tint = VerdeNeon)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val progresso = if (metaDiaria > 0) (ganhoBruto / metaDiaria).toFloat().coerceIn(0f, 1.2f) else 0f
                        LinearProgressIndicator(
                            progress = progresso.coerceAtMost(1f), 
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (progresso >= 1f) VerdeNeon else verdeEsmeralda, 
                            trackColor = textColor.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            item {
                Button(onClick = { viewModel.salvarTurno(context, onSuccess = { Toast.makeText(context, "Turno salvo com sucesso!", Toast.LENGTH_SHORT).show() }) }, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)) {
                    Icon(Icons.Rounded.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FECHAR E SALVAR TURNO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate(); viewModel.alterarDataRegistro(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }; showDatePicker = false }) { Text("OK", color = VerdeNeon) } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = textColor.copy(alpha = 0.5f)) } }) { DatePicker(state = datePickerState) }
    }

    if (showMetaDialog) {
        AlertDialog(
            onDismissRequest = { showMetaDialog = false },
            title = { Text("Ajustar Meta Diária", color = textColor) },
            text = {
                OutlinedTextField(
                    value = metaInputDialog,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) metaInputDialog =
                            it.replace(',', '.')
                    },
                    label = { Text("Valor da Meta (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = textColor,
                        focusedTextColor = textColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor =
                            metaInputDialog.toDoubleOrNull() ?: 0.0; viewModel.updateMetaDiaria(
                        valor,
                        context
                    ); showMetaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeNeon,
                        contentColor = Color.Black
                    )
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showMetaDialog = false }) {
                    Text(
                        "Cancelar",
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAccessibilityDialog) {
        AlertDialog(onDismissRequest = { showAccessibilityDialog = false }, title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Security, contentDescription = null, tint = VerdeNeon); Spacer(modifier = Modifier.width(8.dp)); Text("Ativar RideAssistant", color = textColor) } }, text = { Text("Para capturar ganhos da Uber, 99 e iFood automaticamente, precisamos da permissão de Acessibilidade.\n\n1. Clique em 'Ir para Configurações'\n2. Procure por 'MinhaRota PRO'\n3. Ative a chave de serviço.", color = textColor.copy(alpha = 0.7f)) }, confirmButton = { Button(onClick = { showAccessibilityDialog = false; val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS); context.startActivity(intent) }, colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black), shape = RoundedCornerShape(50.dp)) { Text("Ir para Configurações") } }, dismissButton = { TextButton(onClick = { showAccessibilityDialog = false }) { Text("Agora não", color = textColor.copy(alpha = 0.5f)) } }, containerColor = if (isDark) Color(0xFF1E293B) else Color.White, shape = RoundedCornerShape(24.dp))
    }
}
