package com.raffastudioproducoes.minharota.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.ui.components.CustomBottomNavBarGlow
import com.raffastudioproducoes.minharota.ui.components.DrawerConteudoGradientRainbowV2
import com.raffastudioproducoes.minharota.ui.components.HeaderSuperior
import com.raffastudioproducoes.minharota.ui.components.ModalRegistroRapido
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.screens.auth.AuthScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.LoginScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.RegisterScreen
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
import com.raffastudioproducoes.minharota.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun MainAppContent() {
    val navController: NavHostController = rememberNavController()
    val hojeViewModel: HojeViewModel = viewModel()
    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager(context) }
    
    // Estados Hoisted (Elevados) para persistência entre navegação
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarModalRapido by remember { mutableStateOf(false) }
    val isRidingMode by hojeViewModel.isRidingMode.collectAsState()

    // Controle de Splash/cards (Sempre aparece ao abrir)
    var exibirSplashCard by remember { mutableStateOf(true) }

    // Controle de visibilidade do Scaffold (Header/BottomBar)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val rotasSemScaffold = listOf("login_main", "login_email", "register", "splash", "onboarding")
    val mostrarScaffold = currentRoute != null && !rotasSemScaffold.contains(currentRoute)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> }

    // Rota inicial inteligente
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = if (currentUser != null) Rota.Hoje.route else "login_main"

    // Camada do Splash/Cards (Aparece antes de tudo)
    if (exibirSplashCard) {
        SplashScreen(onFinish = { exibirSplashCard = false })
    } else {

    // Camada do App (Drawer e Scaffold aqui dentro)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (mostrarScaffold) {
                DrawerConteudoGradientRainbowV2(
                    drawerState = drawerState,
                    scope = scope,
                    onNavigate = { route -> navController.navigate(route) },
                    currentRoute = currentRoute ?: "",
                    sharedPreferencesManager = prefsManager,
                    user = User,
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (mostrarScaffold && !isRidingMode) {
                    HeaderSuperior(onDrawerClick = { scope.launch { drawerState.open() } })
                }
            },
            bottomBar = {
                if (mostrarScaffold && !isRidingMode) {
                    CustomBottomNavBarGlow(
                        navController = navController,
                        onFabClick = { mostrarModalRapido = true }
                    )
                }
            }
        ) { paddingValues ->

            NavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = Modifier.padding(if (mostrarScaffold) paddingValues else PaddingValues(0.dp))
            ) {

                // --- ROTAS DE AUTENTICAÇÃO ---
                composable("login_main") {
                    AuthScreen(
                        onAuthSuccess = {
                            val permissions = mutableListOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            permissionLauncher.launch(permissions.toTypedArray())

                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("login_main") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate("register") },
                        onNavigateToEmailLogin = { navController.navigate("login_email") }
                    )
                }

                composable("login_email") {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLoginSuccess = {
                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("login_main") { inclusive = true }
                            }
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("login_main") { inclusive = true }
                            }
                        }
                    )
                }

                // --- ROTAS DO APP (Sem wrappers duplicados!) ---
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
                        onLogout = {
                            navController.navigate("login_main") {
                                popUpTo(Rota.Hoje.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Rota.Plans.route) { PlansScreen() }
                composable(Rota.Configuracoes.route) { ConfigScreen() }
            }
        }
    }
    }
    // Modal de Registro Rápido (Global)
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