package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
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

    // Animar o offset do conteúdo quando o drawer abre/fecha
    val drawerOffsetPx by animateDpAsState(
        targetValue = if (drawerState.isOpen) 280.dp else 0.dp,
        label = "DrawerPushOffset"
    )

    // Animar o arredondamento do canto quando o drawer abre
    val cornerRadius by animateDpAsState(
        targetValue = if (drawerState.isOpen) 24.dp else 0.dp,
        label = "ContentCornerRadius"
    )

    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        // Drawer (Push Navigation)
        Box(
            modifier = Modifier
                .offset(x = -(280.dp - drawerOffsetPx))
                .background(MaterialTheme.colorScheme.background)
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

        // Conteúdo Principal (Deslocado) com Canto Arredondado Dinâmico (Cima e Baixo)
        // Usando a assinatura posicional clássica para evitar erros de compilação conforme diretriz v1.5.0
        Box(
            modifier = Modifier
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

        // Overlay Scrim (quando drawer está aberto)
        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .offset(x = drawerOffsetPx)
                    .clickable {
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
