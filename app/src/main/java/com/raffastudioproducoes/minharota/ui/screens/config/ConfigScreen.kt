package com.raffastudioproducoes.minharota.ui.screens.config

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.raffastudioproducoes.minharota.ui.theme.isAppDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.data.local.SecurePreferences
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import com.raffastudioproducoes.minharota.ui.viewmodel.AppThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.alpha

@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val prefs = SecurePreferences.get(context)
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    val themeViewModel: AppThemeViewModel = viewModel()
    val themeModo by themeViewModel.themeMode.collectAsState()
    val sharedPrefs = SharedPreferencesManager(context)

    // Estados de Configuração persistidos
    val temPlanoPago = remember { sharedPrefs.obterIsPro() }
    var backupAutomatico by remember { mutableStateOf(prefs.getBoolean("backup_automatico", false)) }
    var notificacoesGanhos by remember { mutableStateOf(prefs.getBoolean("notificacoes_ganhos", true)) }
    var diasFolga by remember { mutableStateOf(sharedPrefs.obterDiasFolga()) }

    LaunchedEffect(Unit) {
        themeViewModel.carregarTema(context)
    }
    
    val versaoApp = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "2.0.0"
        } catch (e: Exception) { "2.0.0" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Seção: Aparência
        ConfigSectionTitle("Aparência")
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tema", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        Triple(0, "Automático", Icons.Rounded.Settings),
                        Triple(1, "Claro", Icons.Rounded.LightMode),
                        Triple(2, "Escuro", Icons.Rounded.DarkMode)
                    ).forEach { (modo, label, icon) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (themeModo == modo) VerdeNeon.copy(alpha = 0.2f)
                                    else textColor.copy(alpha = 0.05f)
                                )
                                .clickable {
                                    themeViewModel.mudarTema(context, modo)
                                    Toast.makeText(context, "Tema atualizado", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = null, tint = if (themeModo == modo) VerdeNeon else textColor, modifier = Modifier.size(24.dp))
                            Text(label, color = if (themeModo == modo) VerdeNeon else textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção: Dados e Nuvem
        ConfigSectionTitle("Dados e Sincronização")
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                ConfigSwitchItem(
                    icon = Icons.Rounded.CloudUpload,
                    title = "Backup Automático",
                    subtitle = if (temPlanoPago) "Sincronizar ganhos com a nuvem" else "Disponível em Premium/Pro",
                    checked = backupAutomatico && temPlanoPago,
                    enabled = temPlanoPago,
                    onCheckedChange = {
                        if (temPlanoPago) {
                            backupAutomatico = it
                            prefs.edit().putBoolean("backup_automatico", it).apply()
                        } else {
                            Toast.makeText(context, "Upgrade para Premium ou Pro", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                ConfigClickItem(
                    icon = Icons.Rounded.Sync,
                    title = "Sincronizar Agora",
                    subtitle = "Último backup: Hoje, 14:20",
                    onClick = { Toast.makeText(context, "Sincronizando dados...", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção: Notificações
        ConfigSectionTitle("Notificações")
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            ConfigSwitchItem(
                icon = Icons.Rounded.NotificationsActive,
                title = "Alertas de Ganhos",
                subtitle = "Notificar ao atingir metas diárias",
                checked = notificacoesGanhos,
                onCheckedChange = { 
                    notificacoesGanhos = it
                    prefs.edit().putBoolean("notificacoes_ganhos", it).apply()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ConfigSectionTitle("Dias de folga fixos")
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    diasSemana.forEachIndexed { index, dia ->
                        val numeroDia = index + 1
                        val selecionado = numeroDia in diasFolga
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selecionado) VerdeNeon else textColor.copy(alpha = 0.05f))
                                .clickable {
                                    diasFolga = diasFolga.toMutableSet().also { dias ->
                                        if (selecionado) dias.remove(numeroDia) else dias.add(numeroDia)
                                        SharedPreferencesManager(context).salvarDiasFolga(dias)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dia,
                                color = if (selecionado) Color.Black else textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Dias marcados ficam disponíveis para os avisos de folga e não devem gerar meta automática.",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seção: Sobre
        ConfigSectionTitle("Sobre o App")
        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                ConfigInfoItem(
                    icon = Icons.Rounded.Info,
                    title = "Versão do Sistema",
                    value = versaoApp
                )
                HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                ConfigClickItem(
                    icon = Icons.Rounded.Description,
                    title = "Termos e Privacidade",
                    subtitle = "Leia como protegemos seus dados",
                    onClick = { /* Abrir URL */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ConfigSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = VerdeNeon,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun ConfigSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(if (enabled) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconContainer(icon)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isDark) Color.Black else Color.White,
                checkedTrackColor = VerdeNeon,
                uncheckedThumbColor = textColor.copy(alpha = 0.5f),
                uncheckedTrackColor = textColor.copy(alpha = 0.1f),
                disabledCheckedThumbColor = textColor.copy(alpha = 0.3f),
                disabledCheckedTrackColor = textColor.copy(alpha = 0.1f),
                disabledUncheckedThumbColor = textColor.copy(alpha = 0.2f),
                disabledUncheckedTrackColor = textColor.copy(alpha = 0.05f)
            )
        )
    }
}

@Composable
fun ConfigClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconContainer(icon)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = textColor.copy(alpha = 0.3f))
    }
}

@Composable
fun ConfigInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconContainer(icon)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = value, color = VerdeNeon, fontWeight = FontWeight.Medium, fontSize = 12.sp)
        }
    }
}

@Composable
fun IconContainer(icon: ImageVector) {
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(textColor.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
    }
}
