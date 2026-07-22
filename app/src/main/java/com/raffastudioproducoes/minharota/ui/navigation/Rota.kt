package com.raffastudioproducoes.minharota.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Rota(val route: String, val title: String, val icon: ImageVector) {
    object Hoje : Rota("hoje", "Hoje", Icons.Outlined.Today)
    object Contas : Rota("contas", "Contas", Icons.Outlined.AccountBalanceWallet)
    object Caixas : Rota("caixas", "Caixas", Icons.Outlined.Inventory2)
    object Graficos : Rota("graficos", "Gráficos", Icons.Outlined.BarChart)
    object Garagem : Rota("garagem", "Garagem", Icons.Outlined.TwoWheeler)
    object Extrato : Rota("extrato", "Extrato", Icons.AutoMirrored.Outlined.ReceiptLong) // Atualizado!
    object Dividas : Rota("dividas", "Dívidas", Icons.Outlined.MoneyOff)
    object ContaDiaria : Rota("contadiaria", "Conta Diária", Icons.Outlined.AccountBalanceWallet)
    object Perfil : Rota("perfil", "Perfil", Icons.Outlined.Person)
    object Configuracoes : Rota("configuracoes", "Configurações", Icons.Outlined.Settings)

    object Plans : Rota("plans", "Planos", Icons.Outlined.Star)

    object Ajuda : Rota("ajuda", "Ajuda", Icons.Outlined.Info)
}

val itensNavegacao = listOf(
    Rota.Hoje,
    Rota.Contas,
    Rota.Caixas,
    Rota.Graficos
)
