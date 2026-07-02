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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.TwoWheeler
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
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.theme.CyanBright
import com.raffastudioproducoes.minharota.ui.theme.ElectricBlue
import com.raffastudioproducoes.minharota.ui.theme.OrangeWarm
import com.raffastudioproducoes.minharota.ui.theme.PinkVibrant
import com.raffastudioproducoes.minharota.ui.theme.TealAccent
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada

@Composable
fun DrawerConteudoGradientRainbow(navController: NavController, onClose: () -> Unit) {
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
                Icon(Icons.Outlined.Close, contentDescription = "Fechar Menu")
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

        // Itens Principais com Gradientes Arco-Íris para Transparente
        val mainItems = listOf(
            Triple(Icons.Outlined.Today, "Hoje", Brush.linearGradient(
                colors = listOf(TealAccent.copy(alpha = 0.8f), Color.Transparent)
            )),
            Triple(Icons.Outlined.AccountBalanceWallet, "Contas", Brush.linearGradient(
                colors = listOf(ElectricBlue.copy(alpha = 0.8f), Color.Transparent)
            )),
            Triple(Icons.Outlined.Inventory2, "Caixas", Brush.linearGradient(
                colors = listOf(PinkVibrant.copy(alpha = 0.8f), Color.Transparent)
            )),
            Triple(Icons.Outlined.BarChart, "Gráficos", Brush.linearGradient(
                colors = listOf(VerdeEntrada.copy(alpha = 0.8f), Color.Transparent)
            ))
        )

        mainItems.forEach { (icon, title, gradient) ->
            DrawerItemGradientRainbow(icon, title, gradient) {
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
            text = "Outros",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Itens Secundários com Gradientes Arco-Íris para Transparente
        val secondaryItems = listOf(
            Triple(Icons.Outlined.ReceiptLong, "Extrato", Brush.linearGradient(
                colors = listOf(CyanBright.copy(alpha = 0.8f), Color.Transparent)
            )),
            Triple(Icons.Outlined.MoneyOff, "Dívidas", Brush.linearGradient(
                colors = listOf(OrangeWarm.copy(alpha = 0.8f), Color.Transparent)
            )),
            Triple(Icons.Outlined.TwoWheeler, "Garagem", Brush.linearGradient(
                colors = listOf(VerdeEntrada.copy(alpha = 0.8f), Color.Transparent)
            )),
            Triple(Icons.Outlined.Settings, "Configurações", Brush.linearGradient(
                colors = listOf(ElectricBlue.copy(alpha = 0.8f), Color.Transparent)
            ))
        )

        secondaryItems.forEach { (icon, title, gradient) ->
            DrawerItemGradientRainbow(icon, title, gradient) {
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
fun DrawerItemGradientRainbow(
    icon: ImageVector,
    title: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(56.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(brush = gradient, shape = RoundedCornerShape(28.dp))
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
