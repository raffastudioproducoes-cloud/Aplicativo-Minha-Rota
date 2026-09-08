package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.navigation.itensNavegacao
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

/**
 * Bottom nav estilo "pill" (referência: Pinterest app) — avatar circular à esquerda
 * conectado por uma curva orgânica ao item ativo, que mostra ícone + label empilhados.
 * Demais itens mostram apenas ícone. Cores do app mantidas (verde neon em vez de vermelho).
 */
@Composable
fun BottomNavBarPill(
    navController: NavController,
    onFabClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Índice do item selecionado dentro da barra (itens de navegação + botão "Criar" no fim)
    val totalItens = itensNavegacao.size + 1
    val selectedIndex = itensNavegacao.indexOfFirst { it.route == currentRoute }.let {
        if (it == -1) 0 else it
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // AVATAR CIRCULAR DESTACADO (logo do app — placeholder "M")
        Box(
            modifier = Modifier
                .padding(bottom = 6.dp)
                .size(52.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF141416))
                .border(width = 2.dp, color = VerdeNeon, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("M", color = VerdeNeon, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.width(4.dp))

        // PILL PRINCIPAL COM OS ITENS DE NAVEGAÇÃO + CURVA CONECTORA
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
        ) {
            val itemWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { (maxWidth / totalItens).toPx() }
            val targetCenterX by animateFloatAsState(
                targetValue = itemWidthPx * selectedIndex + itemWidthPx / 2f,
                animationSpec = tween(280),
                label = "navCurveX"
            )

            // Curva orgânica ligando o avatar ao item selecionado
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .padding(bottom = 6.dp)
            ) {
                val startX = -28f
                val startY = size.height * 0.15f
                val endX = targetCenterX
                val endY = size.height * 0.15f

                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        startX + (endX - startX) * 0.35f, startY + size.height * 0.55f,
                        endX - (endX - startX) * 0.35f, startY + size.height * 0.55f,
                        endX, endY
                    )
                }
                drawPath(
                    path = path,
                    color = VerdeNeon,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF141416)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                itensNavegacao.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavPillItem(
                        icon = item.icon,
                        label = item.title,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
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
                    modifier = Modifier.weight(1f),
                    onClick = onFabClick
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) VerdeNeon else Color(0xFF9A9A9E),
        animationSpec = tween(220),
        label = "navItemColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
