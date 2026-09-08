package com.raffastudioproducoes.minharota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.ui.MainAppContent
import com.raffastudioproducoes.minharota.ui.theme.MinhaRotaTema
import com.raffastudioproducoes.minharota.ui.viewmodel.AppThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: AppThemeViewModel = viewModel()
            val context = LocalContext.current
            val systemDark = isSystemInDarkTheme()

            LaunchedEffect(Unit) {
                themeViewModel.carregarTema(context)
            }

            val themeMode by themeViewModel.themeMode.collectAsState()
            val isDark = themeViewModel.isDarkTheme(systemDark)

            MinhaRotaTema(darkTheme = isDark) {
                MainAppContent()
            }
        }
    }
}
