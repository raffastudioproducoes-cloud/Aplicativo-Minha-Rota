package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel

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
            // Chamando o drawer com gradiente que você prefere
            DrawerConteudoGradientRainbow(
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
            // Espaçamento extra aqui para não colidir com o Header ou BottomBar
            Box(modifier = Modifier.padding(paddingValues)) {
                content(PaddingValues(0.dp)) // O padding já está aplicado no Box pai
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
