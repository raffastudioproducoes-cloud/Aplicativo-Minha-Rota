package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.raffastudioproducoes.minharota.ui.navigation.Rota
import com.raffastudioproducoes.minharota.ui.navigation.itensNavegacao
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

/**
 * Bottom nav com "entalhe" dinâmico: a barra ganha uma concavidade que acompanha
 * o item selecionado, cujo ícone fica dentro de um círculo elevado acima da barra.
 * Cores do app mantidas (verde neon + preto).
 */
@Composable
fun BottomNavBarPill(
    navController: NavController,
    onFabClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val totalItens = itensNavegacao.size + 1 // + botão "Criar"
    val selectedIndex = itensNavegacao.indexOfFirst { it.route == currentRoute }.let {
        if (it == -1) totalItens - 1 else it // fallback: nenhum selecionado -> notch no "Criar"
    }

    val barColor = Color(0xFF141416)
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val barWidthPx = with(density) { maxWidth.toPx() }
            val itemWidthPx = barWidthPx / totalItens
            val notchRadiusPx = with(density) { 30.dp.toPx() }

            val targetCenterX by animateFloatAsState(
                targetValue = itemWidthPx * selectedIndex + itemWidthPx / 2f,
                animationSpec = tween(300),
                label = "notchX"
            )

            val barHeight = 66.dp
            val notchDip = with(density) { 34.dp.toPx() }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .shadow(elevation = 10.dp)
            ) {
                val w = size.width
                val h = size.height
                val cornerRadius = h / 2f
                val curveSpread = notchRadiusPx * 1.6f

                val path = Path().apply {
                    moveTo(cornerRadius, 0f)
                    lineTo(targetCenterX - curveSpread, 0f)

                    cubicTo(
                        targetCenterX - curveSpread * 0.55f, 0f,
                        targetCenterX - notchRadiusPx, notchDip,
                        targetCenterX, notchDip
                    )
                    cubicTo(
                        targetCenterX + notchRadiusPx, notchDip,
                        targetCenterX + curveSpread * 0.55f, 0f,
                        targetCenterX + curveSpread, 0f
                    )

                    lineTo(w - cornerRadius, 0f)
                    quadraticTo(w, 0f, w, cornerRadius)
                    lineTo(w, h)
                    lineTo(0f, h)
                    lineTo(0f, cornerRadius)
                    quadraticTo(0f, 0f, cornerRadius, 0f)
                    close()
                }

                drawPath(path = path, color = barColor)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                itensNavegacao.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavNotchItem(
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
                NavNotchItem(
                    icon = Icons.Filled.Add,
                    label = "Criar",
                    isSelected = false,
                    modifier = Modifier.weight(1f),
                    onClick = onFabClick
                )
            }

            // CÍRCULO ELEVADO na posição do item selecionado
            val circleSizeDp = 56.dp
            val circleOffsetX = with(density) { (targetCenterX - with(density) { circleSizeDp.toPx() } / 2f).toDp() }

            Box(
                modifier = Modifier
                    .offset(x = circleOffsetX, y = (-14).dp)
                    .size(circleSizeDp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(VerdeNeon),
                contentAlignment = Alignment.Center
            ) {
                val icon = itensNavegacao.getOrNull(selectedIndex)?.icon ?: Icons.Filled.Add
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun NavNotchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else Color(0xFF9A9A9E),
        animationSpec = tween(200),
        label = "navNotchColor"
    )

    Column(
        modifier = modifier
            .padding(bottom = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isSelected) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            // Espaço reservado: o ícone real aparece no círculo elevado por cima
            Spacer(modifier = Modifier.height(20.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = VerdeNeon,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
