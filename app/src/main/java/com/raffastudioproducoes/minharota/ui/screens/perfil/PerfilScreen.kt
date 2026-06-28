package com.raffastudioproducoes.minharota.ui.screens.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = viewModel(),
    onNavigatePlans: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val nomeUsuario by viewModel.nomeUsuario.collectAsState()
    val email by viewModel.email.collectAsState()
    val dataAniversario by viewModel.dataAniversario.collectAsState()
    val fotoPerfilUrl by viewModel.fotoPerfilUrl.collectAsState()

    // Carregar dados persistidos ao abrir a tela
    LaunchedEffect(Unit) {
        viewModel.carregarDadosPerfil(context)
    }

    var nomeEditavel by remember { mutableStateOf(false) }
    var emailEditavel by remember { mutableStateOf(false) }
    var mostrarMenuFoto by remember { mutableStateOf(false) }

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
            .background(FundoDark)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Meu Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

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
                        // Exibir foto real persistida via Coil AsyncImage
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
                    color = Color.White
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
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
                    Divider(color = Color.White.copy(alpha = 0.05f))
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
                // Nome — pré-populado do ViewModel
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

                // Data de Nascimento — abre DatePickerDialog nativo ao clicar em editar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Data de Nascimento",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f),
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
                        color = if (dataAniversario.isEmpty()) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
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
            colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon)
        ) {
            Text("Ver Planos Premium", fontWeight = FontWeight.Bold, color = Color.Black)
        }

        // Botão Sair
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair da Conta", color = Color(0xFFF87171), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // DatePickerDialog nativo do Material 3 para aniversário
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
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
                    Text("CANCELAR", color = Color.White.copy(alpha = 0.5f))
                }
            }
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
    placeholder: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar $label",
                    modifier = Modifier.size(18.dp),
                    tint = VerdeNeon
                )
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = if (value == "Não informado") "" else value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        } else {
            Text(
                text = value.ifBlank { "Não informado" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Divider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}
