package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun MenuIconTwoLines(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Canvas(
        modifier = modifier
            .size(24.dp)
            .clickable { onClick() }
    ) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.dp.toPx()

        // Linha superior
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(x = width * 0.2f, y = height * 0.35f),
            end = androidx.compose.ui.geometry.Offset(x = width * 0.8f, y = height * 0.35f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Linha inferior
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(x = width * 0.2f, y = height * 0.65f),
            end = androidx.compose.ui.geometry.Offset(x = width * 0.8f, y = height * 0.65f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
