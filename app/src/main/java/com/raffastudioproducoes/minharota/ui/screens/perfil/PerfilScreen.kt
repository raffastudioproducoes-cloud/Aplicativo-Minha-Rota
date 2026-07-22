package com.raffastudioproducoes.minharota.ui.screens.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.viewmodel.GeminiAiViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = viewModel(),
    geminiViewModel: GeminiAiViewModel = viewModel(),
    onNavigatePlans: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val nomeUsuario by viewModel.nomeUsuario.collectAsState()
    val email by viewModel.email.collectAsState()
    val cpf by viewModel.cpf.collectAsState()
    val dataAniversario by viewModel.dataAniversario.collectAsState()
    val fotoPerfilUrl by viewModel.fotoPerfilUrl.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    // Carregar dados persistidos ao abrir a tela
    LaunchedEffect(Unit) {
        viewModel.carregarDadosPerfil(context)
    }

    var nomeEditavel by remember { mutableStateOf(false) }
    var emailEditavel by remember { mutableStateOf(false) }
    var cpfEditavel by remember { mutableStateOf(false) }
    var mostrarMenuFoto by remember { mutableStateOf(false) }
    var mostrarDialogExcluir by remember { mutableStateOf(false) }

    // DatePickerDialog nativo para aniversário
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // URI temporária para a foto da câmera
    val tempPhotoFile = remember { File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg") }
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempPhotoFile
        )
    }

    // Launchers
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.atualizarFotoPerfilUrl(it.toString(), context) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.atualizarFotoPerfilUrl(Uri.fromFile(tempPhotoFile).toString(), context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Linha do Título com o Botão X de Fechar na mesma altura
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meu Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Card Mestre Glassmorphic para Foto e Dados Básicos
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = VerdeNeon.copy(alpha = 0.1f), shape = CircleShape)
                        .clickable { mostrarMenuFoto = !mostrarMenuFoto },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfilUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(fotoPerfilUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de Perfil",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Foto de Perfil",
                            modifier = Modifier.size(80.dp),
                            tint = VerdeNeon
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Alterar Foto",
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .background(VerdeNeon, shape = CircleShape)
                            .padding(4.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = nomeUsuario.ifBlank { "Motorista" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }

        // Menu de Foto Glassmorphic
        if (mostrarMenuFoto) {
            PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            galeriaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            mostrarMenuFoto = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Escolher da Galeria", color = VerdeNeon)
                    }
                    HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                    TextButton(
                        onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(tempPhotoUri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            mostrarMenuFoto = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tirar Foto", color = VerdeNeon)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seção de Dados Detalhados
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Nome
                PerfilInputField(
                    label = "Nome Completo",
                    value = nomeUsuario,
                    isEditing = nomeEditavel,
                    onEditClick = { nomeEditavel = !nomeEditavel },
                    onValueChange = { viewModel.atualizarNomeUsuario(it, context) }
                )

                // E-mail
                PerfilInputField(
                    label = "E-mail de Acesso",
                    value = email,
                    isEditing = emailEditavel,
                    onEditClick = { emailEditavel = !emailEditavel },
                    onValueChange = { viewModel.atualizarEmail(it, context) }
                )

                // CPF
                PerfilInputField(
                    label = "CPF (Para Pagamentos)",
                    value = cpf,
                    isEditing = cpfEditavel,
                    onEditClick = { cpfEditavel = !cpfEditavel },
                    keyboardType = KeyboardType.Number,
                    placeholder = "000.000.000-00",
                    onValueChange = { novoValor ->
                        val apenasDigitos = novoValor.filter { it.isDigit() }.take(11)
                        val cpfFormatado = formatarCpf(apenasDigitos)
                        viewModel.atualizarCpf(cpfFormatado, context)
                    }
                )

                // Data de Nascimento
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Data de Nascimento",
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Selecionar Data",
                                modifier = Modifier.size(18.dp),
                                tint = VerdeNeon
                            )
                        }
                    }
                    Text(
                        text = if (dataAniversario.isEmpty()) "Não informado" else dataAniversario,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (dataAniversario.isEmpty()) textColor.copy(alpha = 0.3f) else textColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão Planos Premium Pílula
        Button(
            onClick = onNavigatePlans,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black)
        ) {
            Text("Ver Planos Premium", fontWeight = FontWeight.Bold)
        }

        // Botão Sair
        TextButton(
            onClick = {
                geminiViewModel.limparInsights()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair da Conta", color = Color(0xFFF87171), fontWeight = FontWeight.Medium)
        }

        // Botão Excluir Conta
        TextButton(
            onClick = { mostrarDialogExcluir = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Excluir Conta Permanentemente",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Diálogo de Confirmação para Excluir Conta
    if (mostrarDialogExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogExcluir = false },
            title = { Text("Excluir Conta", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza absoluta que deseja excluir sua conta? Todos os seus dados salvos na nuvem serão perdidos permanentemente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogExcluir = false
                        viewModel.excluirConta(context) {
                            onLogout()
                        }
                    }
                ) {
                    Text("Excluir", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogExcluir = false }) {
                    Text("Cancelar", color = textColor)
                }
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // DatePickerDialog nativo corrigido para o fuso UTC (evita o bug de voltar 1 dia)
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")) // <--- Correção do fuso horário
                            .toLocalDate()
                        val formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        viewModel.atualizarDataAniversario(formatted, context)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = VerdeNeon, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCELAR", color = textColor.copy(alpha = 0.5f))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PerfilInputField(
    label: String,
    value: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(18.dp),
                    tint = VerdeNeon
                )
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                placeholder = { Text(placeholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                )
            )
        } else {
            Text(
                text = value.ifBlank { "Não informado" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) textColor.copy(alpha = 0.3f) else textColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(color = textColor.copy(alpha = 0.05f))
    }
}

// Função auxiliar para mascarar o CPF no formato 000.000.000-00
private fun formatarCpf(digitos: String): String {
    val sb = StringBuilder()
    for (i in digitos.indices) {
        if (i == 3 || i == 6) sb.append('.')
        else if (i == 9) sb.append('-')
        sb.append(digitos[i])
    }
    return sb.toString()
}