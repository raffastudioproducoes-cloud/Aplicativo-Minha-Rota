package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.raffastudioproducoes.minharota.ui.navigation.itensNavegacao
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.theme.CyanBright
import com.raffastudioproducoes.minharota.ui.theme.ElectricBlue
import com.raffastudioproducoes.minharota.ui.theme.OrangeWarm
import com.raffastudioproducoes.minharota.ui.theme.PinkVibrant
import com.raffastudioproducoes.minharota.ui.theme.TealAccent
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada

@Composable
fun DrawerConteudoGradient(navController: NavController, onClose: () -> Unit) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Fechar Menu")
            }
        }
        
        Text(
            text = "MinhaRota",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Navegação Principal",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Itens Principais com Degradês
        val mainItems = listOf(
            Triple(Icons.Rounded.Today, "Hoje", Brush.linearGradient(colors = listOf(TealAccent, VerdeEntrada))),
            Triple(Icons.Rounded.AccountBalanceWallet, "Contas", Brush.linearGradient(colors = listOf(ElectricBlue, CyanBright))),
            Triple(Icons.Rounded.Inventory2, "Caixas", Brush.linearGradient(colors = listOf(PinkVibrant, OrangeWarm))),
            Triple(Icons.Rounded.BarChart, "Gráficos", Brush.linearGradient(colors = listOf(VerdeEntrada, ElectricBlue)))
        )

        mainItems.forEach { (icon, title, gradient) ->
            DrawerItemGradient(icon, title, gradient) {
                val route = when (title) {
                    "Hoje" -> Rota.Hoje.route
                    "Contas" -> Rota.Contas.route
                    "Caixas" -> Rota.Caixas.route
                    "Gráficos" -> Rota.Graficos.route
                    else -> Rota.Hoje.route
                }
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
                onClose()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Premium",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Item Premium: Conta Diária
        DrawerItemGradient(
            Icons.Rounded.AccountBalanceWallet,
            "⭐ Conta Diária",
            Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669)))
        ) {
            navController.navigate(Rota.ContaDiaria.route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onClose()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Outros",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Itens Secundários com Degradês
        val secondaryItems = listOf(
            Triple(Icons.Rounded.ReceiptLong, "Extrato", Brush.linearGradient(colors = listOf(CyanBright, ElectricBlue))),
            Triple(Icons.Rounded.MoneyOff, "Dívidas", Brush.linearGradient(colors = listOf(OrangeWarm, PinkVibrant))),
            Triple(Icons.Rounded.TwoWheeler, "Garagem", Brush.linearGradient(colors = listOf(VerdeEntrada, TealAccent))),
            Triple(Icons.Rounded.Settings, "Configurações", Brush.linearGradient(colors = listOf(ElectricBlue, VerdeEntrada)))
        )

        secondaryItems.forEach { (icon, title, gradient) ->
            DrawerItemGradient(icon, title, gradient) {
                val route = when (title) {
                    "Extrato" -> Rota.Extrato.route
                    "Dívidas" -> Rota.Dividas.route
                    "Garagem" -> Rota.Garagem.route
                    "Configurações" -> Rota.Configuracoes.route
                    else -> Rota.Hoje.route
                }
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
                onClose()
            }
        }
    }
}

@Composable
fun DrawerItemGradient(
    icon: ImageVector,
    title: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(brush = gradient, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
