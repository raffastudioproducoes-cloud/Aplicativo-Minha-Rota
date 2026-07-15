package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * IMPLEMENTAÇÃO CORRIGIDA DO MENU DRAWER.
 * Resolve erros de compilação, remove seção de aparência e usa ícones nativos.
 */
@Composable
fun DrawerConteudoGradientRainbowV2(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNavigate: (String) -> Unit,
    currentRoute: String,
    user: User?, // <-- Agora recebemos o modelo de dados completo
    sharedPreferencesManager: SharedPreferencesManager,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        modifier = Modifier.fillMaxHeight()
    ) {}
    val context = LocalContext.current
    val isPro = sharedPreferencesManager.obterIsPro()
    val nomeUsuario = sharedPreferencesManager.obterNomeUsuario()
    val fotoPerfilUrl = sharedPreferencesManager.obterFotoPerfilUrl()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    // Obter versão dinamicamente do PackageInfo
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
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Espaçador para o status bar
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp) // Padding geral do cabeçalho
        ) {
            // 1. CONTA
            CategoryHeader("CONTA")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Joga o X para o canto direito
            ) {
                IconButton(onClick = { onClose() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar menu",
                        tint = Color.White
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically // Centraliza foto e texto na mesma altura
                ) {
                    if (fotoPerfilUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(android.net.Uri.parse(fotoPerfilUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Avatar",
                            tint = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB),
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.CenterVertically as Alignment.Vertical)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user?.displayName
                            ?: "Convidado", // Exibe o nome ou 'Convidado' se null
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = if (user?.isPro == true) Color(0xFF4CAF50) else Color.Gray, // Verde se Pro, Cinza se Free
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            //EtiquetaStatus(isPro = user?.isPro ?: false), // Passa o status real do Firebase
                            text = if (user?.isPro == true) "PRO" else "FREE",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 24.dp))

            // 2. NAVEGAÇÃO PRINCIPAL
            CategoryHeader("NAVEGAÇÃO PRINCIPAL")
            DrawerItemPill(
                label = "Hoje",
                icon = Icons.Rounded.Today,
                isSelected = currentRoute == "hoje",
                gradientColors = listOf(Color(0xFF10B981), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("hoje")
                }
            )
            DrawerItemPill(
                label = "Contas",
                icon = Icons.Rounded.AccountBalanceWallet,
                isSelected = currentRoute == "contas",
                gradientColors = listOf(Color(0xFF3B82F6), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("contas")
                }
            )
            DrawerItemPill(
                label = "Caixas",
                icon = Icons.Rounded.Inventory2,
                isSelected = currentRoute == "caixas",
                gradientColors = listOf(Color(0xFFEC4899), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("caixas")
                }
            )
            DrawerItemPill(
                label = "Gráficos",
                icon = Icons.Rounded.BarChart,
                isSelected = currentRoute == "graficos",
                gradientColors = listOf(Color(0xFF10B981), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("graficos")
                }
            )

            // 3. MAIS
            CategoryHeader("MAIS")
            DrawerItemPill(
                label = "Extrato",
                icon = Icons.Rounded.ReceiptLong,
                isSelected = currentRoute == "extrato",
                gradientColors = listOf(Color(0xFF3B82F6), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("extrato")
                }
            )
            DrawerItemPill(
                label = "Dívidas",
                icon = Icons.Rounded.MoneyOff,
                isSelected = currentRoute == "dividas",
                gradientColors = listOf(Color(0xFFEF4444), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("dividas")
                }
            )
            DrawerItemPill(
                label = "Garagem",
                icon = Icons.Rounded.TwoWheeler,
                isSelected = currentRoute == "garagem",
                gradientColors = listOf(Color(0xFFF59E0B), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("garagem")
                }
            )
            DrawerItemPill(
                label = "Configurações",
                icon = Icons.Rounded.Settings,
                isSelected = currentRoute == "configuracoes",
                gradientColors = listOf(Color(0xFF6B7280), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate("configuracoes")
                }
            )

            // 4. SUPORTE
            CategoryHeader("SUPORTE")
            DrawerItemPill(
                label = "Ajuda",
                icon = Icons.Rounded.Help,
                isSelected = false,
                gradientColors = listOf(Color(0xFF3B82F6), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    // Ação de ajuda
                }
            )
            DrawerItemPill(
                label = "Feedback",
                icon = Icons.Rounded.Feedback,
                isSelected = false,
                gradientColors = listOf(Color(0xFF10B981), Color.Transparent),
                onClick = {
                    scope.launch { drawerState.close() }
                    // Ação de feedback
                }
            )

            // 5. SOBRE
            CategoryHeader("SOBRE")
            DrawerItemPill(
                label = "Versão $versionName",
                icon = Icons.Rounded.Info,
                isSelected = false,
                gradientColors = listOf(textColor.copy(alpha = 0.1f), Color.Transparent),
                onClick = {}
            )
        }
    }
}

@Composable
fun EtiquetaStatus(isPro: Boolean) {
    TODO("Not yet implemented")
}


@Composable
fun CategoryHeader(title: String) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    Text(
        text = title,
        color = textColor.copy(alpha = 0.4f),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun DrawerItemPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(colors = gradientColors)
    } else {
        Brush.horizontalGradient(colors = listOf(Color.Transparent, Color.Transparent))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundBrush)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else textColor.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDrawerConteudo() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val prefsManager = remember { SharedPreferencesManager(context) }

    val usuarioTeste = User(
        uid = "123",
        displayName = "Rafael Machado",
        email = "rafael@teste.com",
        isPro = true
    )

    DrawerConteudoGradientRainbowV2(
        drawerState = drawerState,
        scope = scope,
        onNavigate = { route -> },
        currentRoute = "hoje",
        sharedPreferencesManager = prefsManager,
        user = usuarioTeste,
        onClose = { }
    )
}