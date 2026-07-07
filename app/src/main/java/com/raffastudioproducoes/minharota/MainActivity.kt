package com.raffastudioproducoes.minharota

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.raffastudioproducoes.minharota.ui.MainAppContent
import com.raffastudioproducoes.minharota.ui.theme.MinhaRotaTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            // Lemos a preferência do usuário do SharedPreferences
            val prefs = getSharedPreferences("minha_rota_prefs", Context.MODE_PRIVATE)
            val isDarkSaved = prefs.getBoolean("isDarkTheme", isSystemInDarkTheme())
            
            // Passamos o estado direto para o tema.
            MinhaRotaTema(darkTheme = isDarkSaved) {
                MainAppContent()
            }
        }
    }
}
