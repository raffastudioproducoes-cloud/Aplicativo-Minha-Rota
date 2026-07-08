package com.raffastudioproducoes.minharota.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.raffastudioproducoes.minharota.ui.components.*
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.screens.auth.*
import com.raffastudioproducoes.minharota.ui.screens.caixas.CaixasScreen
import com.raffastudioproducoes.minharota.ui.screens.config.ConfigScreen
import com.raffastudioproducoes.minharota.ui.screens.contadiaria.ContaDiariaScreen
import com.raffastudioproducoes.minharota.ui.screens.contas.ContasScreen
import com.raffastudioproducoes.minharota.ui.screens.dividas.DividasScreen
import com.raffastudioproducoes.minharota.ui.screens.extrato.ExtratoScreen
import com.raffastudioproducoes.minharota.ui.screens.garagem.GaragemScreen
import com.raffastudioproducoes.minharota.ui.screens.graficos.GraficosScreen
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeScreen
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import com.raffastudioproducoes.minharota.ui.screens.perfil.PerfilScreen
import com.raffastudioproducoes.minharota.ui.screens.plans.PlansScreen
import kotlinx.coroutines.launch

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val hojeViewModel: HojeViewModel = viewModel()
    
    // Estado do Drawer e Modal
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarModalRapido by remember { mutableStateOf(false) }
    val isRidingMode by hojeViewModel.isRidingMode.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = if (currentUser != null) Rota.Hoje.route else "login_main"

    // O Drawer e o Scaffold ficam FORA do NavHost, assim eles persistem enquanto você navega
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
            
            // NavHost agora fica contido dentro do padding do Scaffold
            NavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("login_main") {
                    AuthScreen(
                        onAuthSuccess = {
                            // (Seus códigos de permissão mantidos aqui)
                            navController.navigate(Rota.Hoje.route) { popUpTo("login_main") { inclusive = true } }
                        },
                        onNavigateToRegister = { navController.navigate("register") },
                        onNavigateToEmailLogin = { navController.navigate("login_email") }
                    )
                }
                
                composable("login_email") {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLoginSuccess = { navController.navigate(Rota.Hoje.route) { popUpTo("login_main") { inclusive = true } } }
                    )
                }
                
                composable("register") {
                    RegisterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onRegisterSuccess = { navController.navigate(Rota.Hoje.route) { popUpTo("login_main") { inclusive = true } } }
                    )
                }
                
                // Rotas Principais (Sem o ScaffoldPrincipalPush aqui!)
                composable(Rota.Hoje.route) { HojeScreen(viewModel = hojeViewModel) }
                composable(Rota.Contas.route) { ContasScreen() }
                composable(Rota.Caixas.route) { CaixasScreen(hojeViewModel = hojeViewModel) }
                composable(Rota.Graficos.route) { GraficosScreen() }
                composable(Rota.Garagem.route) { GaragemScreen() }
                composable(Rota.Extrato.route) { ExtratoScreen() }
                composable(Rota.Dividas.route) { DividasScreen() }
                composable(Rota.ContaDiaria.route) { ContaDiariaScreen() }
                composable(Rota.Perfil.route) { 
                    PerfilScreen(
                        onNavigatePlans = { navController.navigate(Rota.Plans.route) },
                        onLogout = { navController.navigate("login_main") { popUpTo(Rota.Hoje.route) { inclusive = true } } }
                    ) 
                }
                composable(Rota.Plans.route) { PlansScreen() }
                composable(Rota.Configuracoes.route) { ConfigScreen() }
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
