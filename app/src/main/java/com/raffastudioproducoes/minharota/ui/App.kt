package com.raffastudioproducoes.minharota.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.raffastudioproducoes.minharota.ui.components.ScaffoldPrincipalPush
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeViewModel
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.screens.hoje.HojeScreen
import com.raffastudioproducoes.minharota.ui.screens.caixas.CaixasScreen
import com.raffastudioproducoes.minharota.ui.screens.contas.ContasScreen
import com.raffastudioproducoes.minharota.ui.screens.graficos.GraficosScreen
import com.raffastudioproducoes.minharota.ui.screens.garagem.GaragemScreen
import com.raffastudioproducoes.minharota.ui.screens.extrato.ExtratoScreen
import com.raffastudioproducoes.minharota.ui.screens.dividas.DividasScreen
import com.raffastudioproducoes.minharota.ui.screens.perfil.PerfilScreen
import com.raffastudioproducoes.minharota.ui.screens.config.ConfigScreen
import com.raffastudioproducoes.minharota.ui.screens.splash.SplashScreen
import com.raffastudioproducoes.minharota.ui.screens.onboarding.OnboardingScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.AuthScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.LoginScreen
import com.raffastudioproducoes.minharota.ui.screens.auth.RegisterScreen
import com.raffastudioproducoes.minharota.ui.screens.contadiaria.ContaDiariaScreen
import com.raffastudioproducoes.minharota.ui.screens.plans.PlansScreen

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val hojeViewModel: HojeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(onFinish = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("onboarding") {
            OnboardingScreen(onNavigateToLogin = {
                navController.navigate("login_main") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("login_main") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Rota.Hoje.route) {
                        popUpTo("login_main") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToEmailLogin = {
                    navController.navigate("login_email")
                }
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
        composable(Rota.Hoje.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { HojeScreen(viewModel = hojeViewModel) }
            }
        }
        composable(Rota.Contas.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { ContasScreen() }
            }
        }
        composable(Rota.Caixas.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { CaixasScreen(hojeViewModel = hojeViewModel) }
            }
        }
        composable(Rota.Graficos.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { GraficosScreen() }
            }
        }
        composable(Rota.Garagem.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { GaragemScreen() }
            }
        }
        composable(Rota.Extrato.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { ExtratoScreen() }
            }
        }
        composable(Rota.Dividas.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { DividasScreen() }
            }
        }
        composable(Rota.ContaDiaria.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { ContaDiariaScreen() }
            }
        }
        composable(Rota.Perfil.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { 
                    PerfilScreen(
                        onNavigatePlans = {
                            navController.navigate(Rota.Plans.route)
                        },
                        onLogout = {
                            navController.navigate("login_main") {
                                popUpTo(Rota.Hoje.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
        composable(Rota.Plans.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { PlansScreen() }
            }
        }
        composable(Rota.Configuracoes.route) {
            ScaffoldPrincipalPush(navController, hojeViewModel) { padding ->
                Box(modifier = Modifier.padding(padding)) { ConfigScreen() }
            }
        }
    }
}
