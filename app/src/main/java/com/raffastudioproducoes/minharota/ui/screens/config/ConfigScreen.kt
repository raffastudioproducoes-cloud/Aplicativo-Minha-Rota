package com.raffastudioproducoes.minharota.ui.screens.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raffastudioproducoes.minharota.ui.components.PremiumGlassCard
import com.raffastudioproducoes.minharota.ui.theme.FundoDark
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun ConfigScreen() {
    var darkMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        PremiumGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("Tema Escuro", color = Color.White.copy(alpha = 0.9f)) },
                    supportingContent = { Text("Ativar/Desativar modo dark", color = Color.White.copy(alpha = 0.5f)) },
                    leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null, tint = VerdeNeon) },
                    trailingContent = {
                        Switch(
                            checked = darkMode, 
                            onCheckedChange = { darkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = VerdeNeon,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))

                ListItem(
                    headlineContent = { Text("Backup e Restauração", color = Color.White.copy(alpha = 0.9f)) },
                    supportingContent = { Text("Sincronizar dados na nuvem", color = Color.White.copy(alpha = 0.5f)) },
                    leadingContent = { Icon(Icons.Outlined.Backup, contentDescription = null, tint = VerdeNeon) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))

                ListItem(
                    headlineContent = { Text("Sobre o Aplicativo", color = Color.White.copy(alpha = 0.9f)) },
                    supportingContent = { Text("Versão 1.6.0", color = Color.White.copy(alpha = 0.5f)) },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null, tint = VerdeNeon) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "MinhaRota © 2026",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f)
        )
    }
}
