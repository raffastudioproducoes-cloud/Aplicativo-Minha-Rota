package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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
    // 1. Motor nativo do Compose para gerenciar o gesto e o botão Voltar
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarModalRapido by remember { mutableStateOf(false) }
    val isRidingMode by hojeViewModel.isRidingMode.collectAsState()

    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager(context) }

    // 2. Hack Mágico: ModalNavigationDrawer invisível apenas para ancorar o estado sem erro
    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Transparent, // Oculta a sombra padrão
        drawerContent = { Box(modifier = Modifier.width(0.dp)) } // Menu falso vazio
    ) {
        // 3. Lógica de animação do Push customizado
        val isDrawerOpen = drawerState.isOpen || drawerState.isAnimationRunning
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

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            
            // 4. O NOSSO MENU LATERAL REAL (Desliza e fica no fundo)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = -(drawerWidth - drawerOffsetPx))
            ) {
                DrawerConteudoGradientRainbowV2(
                    drawerState = drawerState,
                    scope = scope,
                    onNavigate = { route ->
                        // 1. Fecha o menu lateral de forma suave primeiro
                        scope.launch { drawerState.close() }
                        
                        // 2. A MÁGICA: Roteamento agressivo e blindado!
                        navController.navigate(route) {
                            // Limpa o histórico de telas até a "hoje" para não pesar a memória do celular
                            popUpTo("hoje") {
                                saveState = true
                            }
                            // Evita o bug de abrir a mesma tela duas vezes
                            launchSingleTop = true
                            // Restaura os dados se a tela já foi aberta
                            restoreState = true
                        }
                    },
                    currentRoute = navController.currentDestination?.route ?: "",
                    sharedPreferencesManager = prefsManager
                )
            }

            // 5. O CONTEÚDO DA TELA (Empurrado para a direita com cantos arredondados)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = drawerOffsetPx)
                    .clip(RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius, topEnd = 0.dp, bottomEnd = 0.dp))
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

                // Sombra da tela e captura do toque fora do menu para fechar
                if (isDrawerOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                scope.launch { drawerState.close() }
                            }
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
}
