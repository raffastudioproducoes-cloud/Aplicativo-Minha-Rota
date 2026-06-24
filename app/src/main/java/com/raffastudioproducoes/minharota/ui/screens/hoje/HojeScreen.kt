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
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.util.TextRecognitionHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojeScreen(viewModel: HojeViewModel = viewModel()) {
    val context = LocalContext.current
    
    val dataRegistro by viewModel.dataRegistro.collectAsState()
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
    
    var descCusto by remember { mutableStateOf("") }
    var valorCusto by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var metaDiariaInput by remember { mutableStateOf(if (metaDiaria > 0) metaDiaria.toString() else "") }
    var ganhoBrutoInput by remember { mutableStateOf(if (ganhoBruto > 0) ganhoBruto.toString() else "") }
    
    LaunchedEffect(metaDiaria) {
        if (metaDiaria == 0.0) {
            metaDiariaInput = ""
        } else if (metaDiariaInput.toDoubleOrNull() != metaDiaria) {
            metaDiariaInput = if (metaDiaria % 1.0 == 0.0) metaDiaria.toInt().toString() else metaDiaria.toString()
        }
    }

    LaunchedEffect(ganhoBruto) {
        if (ganhoBruto == 0.0) {
            ganhoBrutoInput = ""
        } else if (ganhoBrutoInput.toDoubleOrNull() != ganhoBruto) {
            ganhoBrutoInput = if (ganhoBruto % 1.0 == 0.0) ganhoBruto.toInt().toString() else ganhoBruto.toString()
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

    Box(modifier = Modifier.fillMaxSize().background(FundoDark)) {
        if (isRidingMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FundoDark),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "R$ ${String.format("%.2f", ganhoLiquido)}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeNeon
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
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon)
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
                    // 1. DASHBOARD CORE PREMIUM
                    PremiumGlassCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ganho Acumulado do Dia",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "R$ ${String.format("%.2f", ganhoLiquido)}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = VerdeNeon
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MiniInfo(label = "Horas", value = horasTrabalhadas)
                                Divider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
                                MiniInfo(label = "R$/Hora", value = "R$ ${String.format("%.2f", valorPorHora)}")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // BOTÃO MODO RIDING PREMIUM
                    Button(
                        onClick = { viewModel.toggleRidingMode() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Rounded.TwoWheeler, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entrar no Modo Riding", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BLOCO 1: DATA DO REGISTRO
                    SectionCard(title = "Data do Registro") {
                        OutlinedTextField(
                            value = dataRegistro,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            enabled = false,
                            label = { Text("Data") },
                            leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.12f),
                                disabledLabelColor = Color.White.copy(alpha = 0.4f),
                                disabledLeadingIconColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
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
                                colors = CheckboxDefaults.colors(checkedColor = VerdeNeon)
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
                            color = VerdeNeon,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BLOCO 3: VALORES DO DIA
                    SectionCard(title = "Valores do Dia") {
                        OutlinedTextField(
                            value = ganhoBrutoInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                    val normalized = input.replace(',', '.')
                                    ganhoBrutoInput = normalized
                                    val v = normalized.toDoubleOrNull() ?: 0.0
                                    viewModel.updateGanhoBruto(v)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Ganho Bruto (R$)") },
                            placeholder = { Text("Ex: 150.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdeNeon,
                                focusedLabelColor = VerdeNeon
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BLOCO 4: META DIÁRIA
                    SectionCard(title = "Meta Diária") {
                        OutlinedTextField(
                            value = metaDiariaInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                    val normalized = input.replace(',', '.')
                                    metaDiariaInput = normalized
                                    val v = normalized.toDoubleOrNull() ?: 0.0
                                    viewModel.updateMetaDiaria(v)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Definir Meta (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdeNeon,
                                focusedLabelColor = VerdeNeon
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun MiniInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
