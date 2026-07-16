package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel

@Composable
fun ScaffoldPrincipalPush(
    navController: NavHostController,
    hojeViewModel: HojeViewModel,
    currentRoute: String?,
    content: @Composable (PaddingValues) -> Unit
) {
    // Substituímos o DrawerState complexo por um booleano simples
    var isDrawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var mostrarModalRapido by remember { mutableStateOf(false) }
    val isRidingMode by hojeViewModel.isRidingMode.collectAsState()

    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager(context) }
    val authRoutes = listOf("login_main", "login_email", "register")
    val showBars = !authRoutes.contains(currentRoute)

    val drawerWidth = 280.dp
    val drawerOffsetPx by animateDpAsState(
        targetValue = if (isDrawerOpen) drawerWidth else 0.dp,
        animationSpec = tween(durationMillis = 350),
        label = "DrawerPushOffset"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isDrawerOpen) 24.dp else 0.dp,
        animationSpec = tween(durationMillis = 350),
        label = "ContentCornerRadius"
    )

    // REMOVEMOS O ModalNavigationDrawer AQUI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // 1. CAMADA DE FUNDO: O Menu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = -(drawerWidth - drawerOffsetPx))
        ) {
            DrawerConteudoGradientRainbowV2(
                modifier = Modifier.fillMaxSize(),
                drawerState = null, // Não usamos mais o estado nativo
                scope = scope,
                onNavigate = { route ->
                    if (navController.currentDestination?.route != route) {
                        navController.navigate(route) {
                            popUpTo(com.raffastudioproducoes.minharota.ui.navigation.Rota.Hoje.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    isDrawerOpen = false // Fecha o menu
                },
                currentRoute = navController.currentDestination?.route ?: "",
                sharedPreferencesManager = prefsManager
            )
        }

        // 2. CAMADA DE FRENTE: O Conteúdo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = drawerOffsetPx)
                .clip(RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius))
        ) {
            Scaffold(
                topBar = {
                    if (!isRidingMode && showBars) {
                        HeaderSuperior(
                            onDrawerClick = { isDrawerOpen = true },
                            drawerState = null // Removido
                        )
                    }
                },
                bottomBar = {
                    if (!isRidingMode && showBars) {
                        BottomNavBarNotch(
                            navController = navController,
                            onFabClick = { mostrarModalRapido = true }
                        )
                    }
                }
            ) { paddingValues ->
                content(paddingValues)
            }

            // 3. CAMADA DE TOQUE (Scrim)
            if (isDrawerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { isDrawerOpen = false }
                )
            }
        }

        if (mostrarModalRapido) {
            ModalRegistroRapido(
                onDismiss = { mostrarModalRapido = false },
                onSave = { valor ->
                    hojeViewModel.registrarGanhoRapido(valor)
                    mostrarModalRapido = false
                }
            )
        }
    }
}