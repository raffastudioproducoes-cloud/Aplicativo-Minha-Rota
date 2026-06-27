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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import java.io.File

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

    var nomeEditavel by remember { mutableStateOf(false) }
    var emailEditavel by remember { mutableStateOf(false) }
    var dataEditavel by remember { mutableStateOf(false) }
    var mostrarMenuFoto by remember { mutableStateOf(false) }
    
    // URI temporária para a foto da câmera
    val tempPhotoFile = remember { File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg") }
    val tempPhotoUri = remember { 
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempPhotoFile
        )
    }

    // Launchers v1.9.1 (Contratos de Resultado Seguros)
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

    // Launcher de Permissão
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
                    if (fotoPerfilUrl.isEmpty()) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Foto de Perfil",
                            modifier = Modifier.size(80.dp),
                            tint = VerdeNeon
                        )
                    } else {
                        // Aqui entraria um AsyncImage (Coil), usando placeholder por enquanto
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
                    text = nomeUsuario,
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

        // Menu de Foto Glassmorphic v1.9.1
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
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
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
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
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
                PerfilInputField(
                    label = "Nome Completo",
                    value = nomeUsuario,
                    isEditing = nomeEditavel,
                    onEditClick = { nomeEditavel = !nomeEditavel },
                    onValueChange = { viewModel.atualizarNomeUsuario(it, context) }
                )

                PerfilInputField(
                    label = "E-mail de Acesso",
                    value = email,
                    isEditing = emailEditavel,
                    onEditClick = { emailEditavel = !emailEditavel },
                    onValueChange = { viewModel.atualizarEmail(it, context) }
                )

                PerfilInputField(
                    label = "Data de Nascimento",
                    value = if (dataAniversario.isEmpty()) "Não informado" else dataAniversario,
                    isEditing = dataEditavel,
                    onEditClick = { dataEditavel = !dataEditavel },
                    onValueChange = { viewModel.atualizarDataAniversario(it, context) },
                    placeholder = "DD/MM/YYYY"
                )
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
                text = value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == "Não informado") Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Divider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}
