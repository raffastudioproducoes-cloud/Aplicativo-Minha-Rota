package com.raffastudioproducoes.minharota.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingPage(
    val title: String,
    val description: String
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Bem-vindo ao MinhaRota",
        description = "Gerencie seus ganhos e despesas de forma inteligente"
    ),
    OnboardingPage(
        title = "Registre seus Turnos",
        description = "Acompanhe cada turno de trabalho com detalhes de ganhos e custos"
    ),
    OnboardingPage(
        title = "Organize com Caixinhas",
        description = "Separe seus ganhos em categorias personalizadas"
    ),
    OnboardingPage(
        title = "Controle sua Garagem",
        description = "Acompanhe manutenções e custos do seu veículo"
    ),
    OnboardingPage(
        title = "Visualize Mapa de Calor",
        description = "Descubra os melhores horários e dias para trabalhar"
    ),
    OnboardingPage(
        title = "Analise Tendências",
        description = "Veja gráficos e estatísticas de desempenho"
    ),
    OnboardingPage(
        title = "Comece Agora",
        description = "Você está pronto para otimizar seus ganhos!"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(onboardingPages.size) { index ->
                HexagonIndicator(isSelected = index == pagerState.currentPage)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pagerState.currentPage == 0) {
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(50.dp))
                        .background(Brush.horizontalGradient(colors = listOf(Color(0xFF06B6D4), Color(0xFF10B981)))),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    contentPadding = PaddingValues()
                ) {
                    Text("Próximo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else if (pagerState.currentPage < onboardingPages.size - 1) {
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor, containerColor = Color.Transparent)
                ) { Text("Voltar", fontWeight = FontWeight.SemiBold) }

                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(50.dp))
                        .background(Brush.horizontalGradient(colors = listOf(Color(0xFF06B6D4), Color(0xFF10B981)))),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    contentPadding = PaddingValues()
                ) { Text("Próximo", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            } else {
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor, containerColor = Color.Transparent)
                ) { Text("Voltar", fontWeight = FontWeight.SemiBold) }

                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("minha_rota_prefs", android.content.Context.MODE_PRIVATE)
                        // v2.0: Usando KEY_IS_FIRST_RUN conforme diretriz de loop
                        prefs.edit().putBoolean("isFirstRun", false).apply()
                        onNavigateToLogin()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                ) { Text("Começar", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)
    
    Box(modifier = Modifier.fillMaxSize()) {
        // v2.0: Fundos Sólidos (Zero Imagens) com Gradiente Suave
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(colors = listOf(Color(0xFF0C0C0E), Color(0xFF111827), Color(0xFF064E3B).copy(alpha = 0.3f)))
                    } else {
                        Brush.verticalGradient(colors = listOf(Color.White, Color(0xFFF3F4F6), Color(0xFFD1FAE5).copy(alpha = 0.3f)))
                    }
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder visual para todas as páginas (Zero Imagens conforme solicitado)
            Box(modifier = Modifier.size(240.dp).drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) VerdeNeon.copy(alpha = 0.2f) else VerdeNeon.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
            })

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun HexagonIndicator(isSelected: Boolean) {
    val isDark = isSystemInDarkTheme()
    val size = if (isSelected) 14.dp else 10.dp
    val color = if (isSelected) Color(0xFF10B981) else (if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f))
    Box(modifier = Modifier.size(size).drawBehind {
        val radius = this.size.width / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val path = Path()
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        if (isSelected) {
            drawPath(path = path, color = color.copy(alpha = 0.3f), style = Stroke(width = 8f))
            drawPath(path = path, color = color)
        } else {
            drawPath(path = path, color = color, style = Stroke(width = 2f))
        }
    })
}
