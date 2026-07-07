package com.raffastudioproducoes.minharota

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.raffastudioproducoes.minharota.ui.MainAppContent
import com.raffastudioproducoes.minharota.ui.theme.MinhaRotaTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            // Lemos a preferência do usuário na abertura
            val prefs = getSharedPreferences("minha_rota_prefs", Context.MODE_PRIVATE)
            val isDarkSaved = prefs.getBoolean("isDarkTheme", true) // Padrão escuro
            
            // Criamos um estado mutável. O Compose "vê" esse estado e redesenha a tela se ele mudar.
            val isDarkTheme = remember { mutableStateOf(isDarkSaved) }

            // AQUI O MÁGICA: Passamos o estado para o tema.
            // *Nota: Precisaremos ajustar o Theme.kt para aceitar esse parâmetro isDark.*
            MinhaRotaTema(isDarkTheme = isDarkTheme.value) {
                MainAppContent()
            }
        }
    }
}
