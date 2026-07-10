package com.raffastudioproducoes.minharota.ui.components

// Importe aqui a sua classe SharedPreferencesManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
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

    // 1. Declarando o contexto e o gerenciador de prefs que faltavam
    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager(context) }

    // 2. Obtendo a rota atual de forma reativa
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerConteudoGradientRainbowV2(
                drawerState = drawerState,
                scope = scope,
                onNavigate = { route -> navController.navigate(route) },
                currentRoute = currentRoute ?: "",
                sharedPreferencesManager = prefsManager,
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
