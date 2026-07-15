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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * SCAFFOLD PRINCIPAL COM EFEITO PUSH (v2.2.0)
 * Refatorado para suportar elevação de estado (State Hoisting).
 * Agora recebe os estados de navegação como parâmetros para evitar reinicialização.
 */
@Composable
fun ScaffoldPrincipalPush(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    isRidingMode: Boolean,
    mostrarHeader: Boolean,
    mostrarBottomBar: Boolean,
    onFabClick: () -> Unit,
    prefsManager: SharedPreferencesManager,
    content: @Composable (PaddingValues) -> Unit,
    user: User?
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Transparent, 
        drawerContent = { Box(modifier = Modifier.width(0.dp)) } 
    ) {
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

        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            
            // MENU DRAWER (Fica "atrás" do conteúdo e faz o push)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = -(drawerWidth - drawerOffsetPx))
            ) {
                DrawerConteudoGradientRainbowV2(
                    drawerState = drawerState,
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
                        scope.launch { drawerState.close() }
                    },
                    currentRoute = navController.currentDestination?.route ?: "",
                    sharedPreferencesManager = prefsManager,
                    user = user,
                    onClose = { scope.launch { drawerState.close() } }
                )
            }

            // CONTEÚDO PRINCIPAL (Faz o push para a direita)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = drawerOffsetPx)
                    .clip(
                        RoundedCornerShape(
                            topStart = cornerRadius,
                            bottomStart = cornerRadius,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
            ) {
                Scaffold(
                    topBar = {
                        if (mostrarHeader && !isRidingMode) {
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
                        if (mostrarBottomBar && !isRidingMode) {
                            BottomNavBarNotch(
                                navController = navController,
                                onFabClick = onFabClick
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    content(paddingValues)
                }

                // Camada de bloqueio/clique para fechar o drawer quando aberto
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
        }
    }
}
