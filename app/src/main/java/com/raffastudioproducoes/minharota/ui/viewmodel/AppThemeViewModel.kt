package com.raffastudioproducoes.minharota.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.raffastudioproducoes.minharota.data.local.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppThemeViewModel : ViewModel() {
    private val _themeMode = MutableStateFlow(0) // 0=Auto, 1=Light, 2=Dark
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun carregarTema(context: Context) {
        val prefs = SecurePreferences.get(context)
        val modo = prefs.getInt("theme_mode", 0) // default: Auto
        _themeMode.value = modo
    }

    fun mudarTema(context: Context, modo: Int) {
        _themeMode.value = modo
        val prefs = SecurePreferences.get(context)
        prefs.edit().putInt("theme_mode", modo).apply()
    }

    @Composable
    fun isDarkTheme(): Boolean = when (_themeMode.value) {
        1 -> false // Light
        2 -> true  // Dark
        else -> isSystemInDarkTheme() // Auto
    }
}
