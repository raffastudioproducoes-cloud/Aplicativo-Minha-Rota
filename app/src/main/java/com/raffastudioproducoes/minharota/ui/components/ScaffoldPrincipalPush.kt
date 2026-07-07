package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
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
import kotlinx.coroutines.launch

@Composable
fun ScaffoldPrincipalPush(
    navController: NavHostController,
    hojeViewModel: HojeViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarModalRapido by remember { mutableStateOf(false) }
    val isRidingMode by hojeViewModel.isRidingMode.collectAsState()

    // Lógica de animação do Push e cantos arredondados
    val isDrawerOpen = drawerState.isOpen || drawerState.isAnimationRunning
    val drawerOffsetPx by animateDpAsState(
        targetValue = if (isDrawerOpen) 280.dp else 0.dp,
        label = "DrawerPushOffset"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isDrawerOpen) 24.dp else 0.dp,
        label = "ContentCornerRadius"
    )

    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager(context) }

    // O "motor" restaurado: ModalNavigationDrawer fornece as âncoras para o gesto
    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.32f), // Fundo escurecido que fecha ao tocar
        drawerContent = {
            DrawerConteudoGradientRainbowV2(
                drawerState = drawerState,
                scope = scope,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                currentRoute = navController.currentDestination?.route ?: "",
                sharedPreferencesManager = prefsManager
            )
        }
    ) {
        // Conteúdo Principal que será empurrado para o lado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .offset(x = drawerOffsetPx)
                // Usando a assinatura posicional para obedecer ao protocolo anti-quebra (topStart, topEnd, bottomEnd, bottomStart)
                .clip(RoundedCornerShape(cornerRadius, 0.dp, 0.dp, cornerRadius))
        ) {
            Scaffold(
                topBar = {
                    if (!isRidingMode) {
                        HeaderSuperior(
                            onDrawerClick = {
                                scope.launch {
                                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                }
                            },
                            drawerState = drawerState
                        )
                    }
                },
                bottomBar = {
                    if (!isRidingMode) {
                        BottomNavBarNotch(
                            navController = navController,
                            onFabClick = { mostrarModalRapido = true }
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                content(paddingValues)
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
