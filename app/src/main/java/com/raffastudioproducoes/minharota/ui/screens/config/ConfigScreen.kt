package com.raffastudioproducoes.minharota.ui.screens.config

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("minha_rota_prefs", Context.MODE_PRIVATE)
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    
    // Estados de Configuração persistidos
    var temaEscuro by remember { mutableStateOf(prefs.getBoolean("tema_escuro", true)) }
    var backupAutomatico by remember { mutableStateOf(prefs.getBoolean("backup_automatico", true)) }
    var notificacoesGanhos by remember { mutableStateOf(prefs.getBoolean("notificacoes_ganhos", true)) }
    
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
            Column {
                ConfigSwitchItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Tema Escuro",
                    subtitle = "Otimizado para visão noturna",
                    checked = temaEscuro,
                    onCheckedChange = { 
                        temaEscuro = it
                        prefs.edit().putBoolean("tema_escuro", it).apply()
                        Toast.makeText(context, "Tema atualizado", Toast.LENGTH_SHORT).show()
                    }
                )
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
                    subtitle = "Sincronizar ganhos com a nuvem",
                    checked = backupAutomatico,
                    onCheckedChange = { 
                        backupAutomatico = it
                        prefs.edit().putBoolean("backup_automatico", it).apply()
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
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
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
            Text(text = subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isDark) Color.Black else Color.White,
                checkedTrackColor = VerdeNeon,
                uncheckedThumbColor = textColor.copy(alpha = 0.5f),
                uncheckedTrackColor = textColor.copy(alpha = 0.1f)
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
    val isDark = isSystemInDarkTheme()
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
    val isDark = isSystemInDarkTheme()
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
    val isDark = isSystemInDarkTheme()
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
