package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
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

    // Usar derivedStateOf para garantir recomposição reativa ao estado do drawer
    val isDrawerOpen by remember { derivedStateOf { drawerState.currentValue == DrawerValue.Open } }

    // Animar o offset do conteúdo quando o drawer abre/fecha
    val drawerOffsetPx by animateDpAsState(
        targetValue = if (isDrawerOpen) 280.dp else 0.dp,
        label = "DrawerPushOffset"
    )

    // Animar o arredondamento do canto quando o drawer abre
    val cornerRadius by animateDpAsState(
        targetValue = if (isDrawerOpen) 24.dp else 0.dp,
        label = "ContentCornerRadius"
    )

    // fillMaxSize garante que o Box ocupe toda a tela e o drawer tenha espaço para aparecer
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Drawer (Push Navigation) — posicionado à esquerda, começa fora da tela
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = -(280.dp - drawerOffsetPx))
        ) {
            val context = LocalContext.current
            val prefsManager = SharedPreferencesManager(context)
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

        // Conteúdo Principal (Deslocado) com Canto Arredondado Dinâmico
        // Usando a assinatura posicional clássica para evitar erros de compilação conforme diretriz v1.5.0
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = drawerOffsetPx)
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

        // Overlay Scrim (quando drawer está aberto) — fecha ao tocar fora
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = drawerOffsetPx)
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        scope.launch { drawerState.close() }
                    }
            )
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
