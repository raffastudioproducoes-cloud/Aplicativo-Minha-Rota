package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.raffastudioproducoes.minharota.ui.theme.isAppDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Drawer estilo "lista simples" (referência: layout com foto + Welcome/Nome + lista de itens
 * ícone-texto, sem pills coloridos). Cores do app mantidas.
 */
@Composable
fun DrawerConteudoGradientRainbowV2(
    drawerState: DrawerState?,
    scope: CoroutineScope,
    onNavigate: (String) -> Unit,
    currentRoute: String,
    sharedPreferencesManager: SharedPreferencesManager,
    modifier: Modifier
) {
    val context = LocalContext.current
    val isPro = sharedPreferencesManager.obterIsPro()
    val nomeUsuario = sharedPreferencesManager.obterNomeUsuario()
    val fotoPerfilUrl = sharedPreferencesManager.obterFotoPerfilUrl()
    val scrollState = rememberScrollState()
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    val versionName = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "2.0.0"
        } catch (e: Exception) { "2.0.0" }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // CABEÇALHO: foto à esquerda + "Bem-vindo" / nome, como na referência
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable {
                    scope.launch { drawerState?.close() }
                    onNavigate("perfil")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(android.net.Uri.parse(fotoPerfilUrl))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Avatar",
                        tint = VerdeNeon,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Bem-vindo" + if (isPro) " · PRO" else "",
                    color = VerdeNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = nomeUsuario.ifBlank { "Motorista" },
                    color = textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            DrawerRowItem(
                label = "Hoje",
                icon = Icons.Outlined.CalendarMonth,
                isSelected = currentRoute == Rota.Hoje.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Hoje.route) }
            )
            DrawerRowItem(
                label = "Contas",
                icon = Icons.Outlined.AccountBalanceWallet,
                isSelected = currentRoute == Rota.Contas.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Contas.route) }
            )
            DrawerRowItem(
                label = "Caixas",
                icon = Icons.Outlined.Inventory2,
                isSelected = currentRoute == Rota.Caixas.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Caixas.route) }
            )
            DrawerRowItem(
                label = "Gráficos",
                icon = Icons.Outlined.BarChart,
                isSelected = currentRoute == Rota.Graficos.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Graficos.route) }
            )
            DrawerRowItem(
                label = "Extrato",
                icon = Icons.Outlined.ReceiptLong,
                isSelected = currentRoute == Rota.Extrato.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Extrato.route) }
            )
            DrawerRowItem(
                label = "Dívidas",
                icon = Icons.Outlined.MoneyOff,
                isSelected = currentRoute == Rota.Dividas.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Dividas.route) }
            )
            DrawerRowItem(
                label = "Garagem",
                icon = Icons.Outlined.TwoWheeler,
                isSelected = currentRoute == Rota.Garagem.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Garagem.route) }
            )
            DrawerRowItem(
                label = "Planos",
                icon = Icons.Outlined.Star,
                isSelected = currentRoute == Rota.Plans.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Plans.route) }
            )
            DrawerRowItem(
                label = "Configurações",
                icon = Icons.Outlined.Settings,
                isSelected = currentRoute == Rota.Configuracoes.route,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Configuracoes.route) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))

            DrawerRowItem(
                label = "Ajuda",
                icon = Icons.Outlined.Help,
                isSelected = false,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Ajuda.route) }
            )
            DrawerRowItem(
                label = "Feedback",
                icon = Icons.Outlined.Feedback,
                isSelected = false,
                onClick = { scope.launch { drawerState?.close() }; onNavigate(Rota.Ajuda.route) }
            )
        }

        HorizontalDivider(color = textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Versão $versionName",
                color = textColor.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DrawerRowItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isDark = isAppDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val contentColor = if (isSelected) VerdeNeon else textColor.copy(alpha = 0.65f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
