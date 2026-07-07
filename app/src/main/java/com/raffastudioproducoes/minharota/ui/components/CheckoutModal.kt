package com.raffastudioproducoes.minharota.ui.components

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// Constantes de preço base
// ─────────────────────────────────────────────────────────────────────────────
private const val PRECO_PREMIUM = 9.99
private const val PRECO_PRO = 19.99

enum class CicloFaturamento(val meses: Int, val label: String, val desconto: Double) {
    UM_MES(1, "1 Mês", 0.0),
    SEIS_MESES(6, "6 Meses", 0.05),
    UM_ANO(12, "1 Ano", 0.10)
}

enum class MetodoPagamento(val label: String, val icon: ImageVector) {
    PIX("Pix", Icons.Rounded.Payments),
    CARTAO("Cartão", Icons.Rounded.CreditCard),
    GOOGLE_PAY("Google Pay", Icons.Rounded.Smartphone)
}

// ─────────────────────────────────────────────────────────────────────────────
// Modal principal de Checkout
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutModal(
    nomePlano: String,           // "Premium" ou "Pro"
    onDismiss: () -> Unit,
    onSuccess: () -> Unit        // callback após confirmação
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    val surfaceAlpha = if (isDark) 0.18f else 0.85f

    val precoBase = if (nomePlano == "Pro") PRECO_PRO else PRECO_PREMIUM

    var cicloSelecionado by remember { mutableStateOf(CicloFaturamento.UM_MES) }
    var metodoPagamento by remember { mutableStateOf(MetodoPagamento.PIX) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    if (isDark)
                        Color(0xFF1C1C1E).copy(alpha = surfaceAlpha)
                    else
                        Color.White.copy(alpha = surfaceAlpha)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF42D789).copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.08f),
                            Color(0xFF42D789).copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle visual
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textColor.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = "Plano $nomePlano",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF42D789)
                )
                Text(
                    text = "Escolha seu ciclo de acesso",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── ETAPA A: Ciclos de Faturamento ──────────────────────────
                Text(
                    text = "CICLO DE FATURAMENTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                CicloFaturamento.entries.forEach { ciclo ->
                    CicloCard(
                        ciclo = ciclo,
                        precoBase = precoBase,
                        isSelected = cicloSelecionado == ciclo,
                        textColor = textColor,
                        isDark = isDark,
                        onClick = { cicloSelecionado = ciclo }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── ETAPA B: Método de Pagamento ─────────────────────────────
                Text(
                    text = "MÉTODO DE PAGAMENTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetodoPagamento.entries.forEach { metodo ->
                        MetodoCard(
                            metodo = metodo,
                            isSelected = metodoPagamento == metodo,
                            textColor = textColor,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { metodoPagamento = metodo }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Resumo do valor ──────────────────────────────────────────
                val valorTotal = calcularValor(precoBase, cicloSelecionado)
                val textoResumo = buildString {
                    append("R$ ${String.format("%.2f", valorTotal)}")
                    if (cicloSelecionado.desconto > 0) {
                        append(" • ${(cicloSelecionado.desconto * 100).toInt()}% de desconto")
                    }
                }
                Text(
                    text = textoResumo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Botão de Confirmação ─────────────────────────────────────
                Button(
                    onClick = {
                        confirmarPagamento(
                            context = context,
                            nomePlano = nomePlano,
                            ciclo = cicloSelecionado
                        )
                        showSuccessDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42D789),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Confirmar Pagamento",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aviso de pagamento único
                Text(
                    text = "Pagamento único • Sem renovação automática",
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Diálogo de sucesso
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSuccess()
                onDismiss()
            },
            title = { Text("Pagamento Confirmado!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Seu plano $nomePlano foi ativado com sucesso! " +
                    "Acesso válido por ${cicloSelecionado.meses} " +
                    if (cicloSelecionado.meses == 1) "mês." else "meses."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSuccess()
                    onDismiss()
                }) {
                    Text("Ótimo!", color = Color(0xFF42D789), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color.White
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card de ciclo de faturamento
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CicloCard(
    ciclo: CicloFaturamento,
    precoBase: Double,
    isSelected: Boolean,
    textColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF42D789) else textColor.copy(alpha = 0.12f),
        animationSpec = tween(200),
        label = "CicloBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            Color(0xFF42D789).copy(alpha = if (isDark) 0.15f else 0.08f)
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "CicloBg"
    )

    val valorTotal = calcularValor(precoBase, ciclo)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF42D789),
                    unselectedColor = textColor.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = ciclo.label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = textColor
                )
                if (ciclo.desconto > 0) {
                    Text(
                        text = "Economize ${(ciclo.desconto * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color(0xFF42D789),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Text(
            text = "R$ ${String.format("%.2f", valorTotal)}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = if (isSelected) Color(0xFF42D789) else textColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card de método de pagamento
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MetodoCard(
    metodo: MetodoPagamento,
    isSelected: Boolean,
    textColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF42D789) else textColor.copy(alpha = 0.12f),
        animationSpec = tween(200),
        label = "MetodoBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            Color(0xFF42D789).copy(alpha = if (isDark) 0.15f else 0.08f)
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "MetodoBg"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = metodo.icon,
            contentDescription = metodo.label,
            tint = if (isSelected) Color(0xFF42D789) else textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = metodo.label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF42D789) else textColor.copy(alpha = 0.6f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun calcularValor(precoBase: Double, ciclo: CicloFaturamento): Double {
    val total = precoBase * ciclo.meses
    return total * (1.0 - ciclo.desconto)
}

private fun confirmarPagamento(context: Context, nomePlano: String, ciclo: CicloFaturamento) {
    val prefs = SharedPreferencesManager(context)
    val dataVencimento = LocalDate.now().plusMonths(ciclo.meses.toLong())
    prefs.salvarIsPro(true)
    prefs.salvarNomePlano(nomePlano)
    prefs.salvarDataVencimento(dataVencimento.format(DateTimeFormatter.ISO_LOCAL_DATE))
}
