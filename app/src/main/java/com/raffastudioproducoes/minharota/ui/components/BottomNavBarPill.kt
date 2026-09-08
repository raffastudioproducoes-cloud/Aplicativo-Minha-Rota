package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.navigation.itensNavegacao
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

/**
 * Bottom nav estilo "pill" inspirado no app Pinterest: barra escura arredondada,
 * avatar circular destacado à esquerda, item ativo com fundo pill verde neon.
 */
@Composable
fun BottomNavBarPill(
    navController: NavController,
    onFabClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // AVATAR CIRCULAR DESTACADO (logo do app)
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF141416))
                .border(width = 2.dp, color = VerdeNeon, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("M", color = VerdeNeon, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }

        // PILL PRINCIPAL COM OS ITENS DE NAVEGAÇÃO
        Row(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF141416))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            itensNavegacao.forEach { item ->
                val isSelected = currentRoute == item.route
                NavPillItem(
                    icon = item.icon,
                    label = item.title,
                    isSelected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(item.route) {
                                popUpTo(Rota.Hoje.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            // BOTÃO CRIAR (equivalente ao "Create" do Pinterest)
            NavPillItem(
                icon = Icons.Filled.Add,
                label = "Criar",
                isSelected = false,
                onClick = onFabClick
            )
        }
    }
}

@Composable
private fun NavPillItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) VerdeNeon else Color.Transparent,
        animationSpec = tween(220),
        label = "navPillBg"
    )
    val contentColor = if (isSelected) Color.Black else Color(0xFF9A9A9E)
    val horizontalPad by animateDpAsState(
        targetValue = if (isSelected) 14.dp else 10.dp,
        animationSpec = tween(220),
        label = "navPillPad"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontalPad, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        if (isSelected) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
