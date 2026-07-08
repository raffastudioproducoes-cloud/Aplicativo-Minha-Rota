package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import kotlinx.coroutines.launch

@Composable
fun ScaffoldPrincipal(
    navController: NavHostController,
    hojeViewModel: HojeViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarModalRapido by remember { mutableStateOf(false) }
    val isRidingMode by hojeViewModel.isRidingMode.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerConteudoGradientRainbow(
                navController = navController,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (!isRidingMode) {
                    HeaderSuperior(onDrawerClick = { scope.launch { drawerState.open() } })
                }
            },
            bottomBar = {
                if (!isRidingMode) {
                    CustomBottomNavBarGlow(
                        navController = navController,
                        onFabClick = { mostrarModalRapido = true }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                content(PaddingValues(0.dp))
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
