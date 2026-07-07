package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.navigation.itensNavegacao
import com.raffastudioproducoes.minharota.ui.theme.CyanBright
import com.raffastudioproducoes.minharota.ui.theme.ElectricBlue
import com.raffastudioproducoes.minharota.ui.theme.VerdeEntrada

@Composable
fun BottomNavBarNotch(
    navController: NavController,
    onFabClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        // NavBar Principal
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(70.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                itensNavegacao.forEachIndexed { index, item ->
                    // Espaço central para o FAB
                    if (index == 2) {
                        Spacer(modifier = Modifier.width(70.dp))
                    }
                    
                    val isSelected = currentRoute == item.route
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Halo Retangular Suave para o item ativo
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 32.dp)
                                    .background(
                                        color = ElectricBlue.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                        
                        IconButton(onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    // CORREÇÃO: Agora usa a tela Hoje como âncora, e não o Splash!
                                    popUpTo(Rota.Hoje.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) ElectricBlue else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // FAB Squircle (Quadrado Arredondado) encaixado
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 0.dp)
                .size(56.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(VerdeEntrada, CyanBright)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onFabClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Adicionar",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Black
                )
            }
        }
    }
}
