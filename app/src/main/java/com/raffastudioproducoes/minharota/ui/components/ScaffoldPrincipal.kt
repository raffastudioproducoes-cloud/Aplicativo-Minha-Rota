package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

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
            DrawerConteudo(
                navController = navController,
                onClose = {
                    scope.launch { drawerState.close() }
                }
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
            content(paddingValues)
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
