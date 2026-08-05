package com.raffastudioproducoes.minharota.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.raffastudioproducoes.minharota.data.local.DataMigrationManager
import com.raffastudioproducoes.minharota.ui.components.ScaffoldPrincipalPush
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.screens.auth.AuthScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.LoginScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.RegisterScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.EmailVerificationScreen
import com.raffastudioproducoes.minharota.repository.auth.SessionDestination
import com.raffastudioproducoes.minharota.repository.auth.restoreSessionDestination
import com.raffastudioproducoes.minharota.ui.screens.caixas.CaixasScreen
import com.raffastudioproducoes.minharota.ui.screens.config.ConfigScreen
import com.raffastudioproducoes.minharota.ui.screens.contadiaria.ContaDiariaScreen
import com.raffastudioproducoes.minharota.ui.screens.contas.ContasScreen
import com.raffastudioproducoes.minharota.ui.screens.dividas.DividasScreen
import com.raffastudioproducoes.minharota.ui.screens.extrato.ExtratoScreen
import com.raffastudioproducoes.minharota.ui.screens.garagem.GaragemScreen
import com.raffastudioproducoes.minharota.ui.screens.graficos.GraficosScreen
import com.raffastudioproducoes.minharota.ui.screens.help.HelpScreen
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeScreen
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import com.raffastudioproducoes.minharota.ui.screens.perfil.PerfilScreen
import com.raffastudioproducoes.minharota.ui.screens.plans.PlansScreen
import com.raffastudioproducoes.minharota.ui.viewmodel.UserViewModel

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val hojeViewModel: HojeViewModel = viewModel()

    // Captura a rota atual para passar para o Scaffold
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    // Rota inicial inteligente: se já estiver logado, pula o login e vai pro Hoje!
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startRoute = when (
        restoreSessionDestination(
            uid = currentUser?.uid,
            displayName = currentUser?.displayName,
            isEmailVerified = currentUser?.isEmailVerified ?: false,
            providerIds = currentUser?.providerData?.mapTo(mutableSetOf()) { it.providerId }.orEmpty()
        )
    ) {
        SessionDestination.LOGIN -> "login_main"
        SessionDestination.VERIFY_EMAIL -> "verify_email"
        SessionDestination.COMPLETE_PROFILE -> "complete_profile"
        SessionDestination.MAIN -> Rota.Hoje.route
    }

    ScaffoldPrincipalPush(
        navController = navController,
        hojeViewModel = hojeViewModel,
        currentRoute = currentRoute
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable("login_main") {
                val userViewModel: UserViewModel = viewModel()
                val context = LocalContext.current

                AuthScreen(
                    onAuthSuccess = {
                        // Executa a migração do perfil (e dados Pro/Premium, se houver) para o Firestore
                        DataMigrationManager.migrarDadosDoConvidadoParaNuvem(context) {
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
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onNavigateToEmailLogin = {
                        navController.navigate("login_email")
                    },
                    userViewModel = userViewModel
                )
            }

            composable("login_email") {
                val context = LocalContext.current
                LoginScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        DataMigrationManager.migrarDadosDoConvidadoParaNuvem(context) {
                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("login_main") { inclusive = true }
                            }
                        }
                    },
                    onProfileCompletionRequired = {
                        navController.navigate("complete_profile") {
                            popUpTo("login_email") { inclusive = true }
                        }
                    },
                    onEmailVerificationRequired = {
                        navController.navigate("verify_email") {
                            popUpTo("login_email") { inclusive = true }
                        }
                    }
                )
            }

            composable("register") {
                val context = LocalContext.current
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        DataMigrationManager.migrarDadosDoConvidadoParaNuvem(context) {
                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("login_main") { inclusive = true }
                            }
                        }
                    },
                    onEmailVerificationRequired = {
                        navController.navigate("verify_email") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            composable("complete_profile") {
                val context = LocalContext.current
                RegisterScreen(
                    onNavigateBack = {},
                    onRegisterSuccess = {
                        DataMigrationManager.migrarDadosDoConvidadoParaNuvem(context) {
                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("complete_profile") { inclusive = true }
                            }
                        }
                    },
                    onEmailVerificationRequired = {
                        navController.navigate("verify_email") {
                            popUpTo("complete_profile") { inclusive = true }
                        }
                    },
                    profileCompletionOnly = true
                )
            }

            composable("verify_email") {
                val context = LocalContext.current
                EmailVerificationScreen(
                    onMainAuthorized = {
                        DataMigrationManager.migrarDadosDoConvidadoParaNuvem(context) {
                            navController.navigate(Rota.Hoje.route) {
                                popUpTo("verify_email") { inclusive = true }
                            }
                        }
                    },
                    onProfileCompletionRequired = {
                        navController.navigate("complete_profile") {
                            popUpTo("verify_email") { inclusive = true }
                        }
                    },
                    onLogout = {
                        navController.navigate("login_main") {
                            popUpTo("verify_email") { inclusive = true }
                        }
                    }
                )
            }

            // --- ROTAS DO APP ---
            composable(Rota.Hoje.route) {
                HojeScreen(viewModel = hojeViewModel)
            }
            composable(Rota.Contas.route) {
                ContasScreen(
                    onNavigateToPlans = {
                        navController.navigate("plans")
                    }
                )
            }
            composable(Rota.Caixas.route) {
                CaixasScreen(
                    hojeViewModel = hojeViewModel,
                    onNavigateToPlans = {
                        navController.navigate("plans")
                    }
                )
            }
            composable(Rota.Graficos.route) {
                GraficosScreen()
            }
            composable(Rota.Garagem.route) {
                GaragemScreen()
            }
            composable(Rota.Extrato.route) {
                ExtratoScreen()
            }
            composable(Rota.Dividas.route) {
                DividasScreen()
            }
            composable(Rota.ContaDiaria.route) {
                ContaDiariaScreen()
            }
            composable(Rota.Perfil.route) {
                PerfilScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigatePlans = {
                        navController.navigate("plans")
                    },
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login_main") {
                            popUpTo(Rota.Hoje.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Rota.Plans.route) {
                PlansScreen(
                    onBack = {
                        navController.popBackStack() // <--- Retorna para a tela anterior instantaneamente
                    }
                )
            }
            composable(Rota.Configuracoes.route) {
                ConfigScreen()
            }
            composable("ajuda") {
                HelpScreen(
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
