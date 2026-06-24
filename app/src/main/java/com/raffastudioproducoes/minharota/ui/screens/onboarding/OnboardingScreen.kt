package com.raffastudioproducoes.minharota.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.R
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingPage(
    val title: String,
    val description: String,
    val bgRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Bem-vindo ao MinhaRota",
        description = "Gerencie seus ganhos e despesas de forma inteligente",
        bgRes = R.drawable.bg_onb_carro
    ),
    OnboardingPage(
        title = "Registre seus Turnos",
        description = "Acompanhe cada turno de trabalho com detalhes de ganhos e custos",
        bgRes = R.drawable.bg_onb_calendario
    ),
    OnboardingPage(
        title = "Organize com Caixinhas",
        description = "Separe seus ganhos em categorias personalizadas",
        bgRes = R.drawable.bg_onb_caixas
    ),
    OnboardingPage(
        title = "Controle sua Garagem",
        description = "Acompanhe manutenções e custos do seu veículo",
        bgRes = R.drawable.bg_onb_engrenagens
    ),
    OnboardingPage(
        title = "Visualize Mapa de Calor",
        description = "Descubra os melhores horários e dias para trabalhar",
        bgRes = R.drawable.bg_onb_mapa
    ),
    OnboardingPage(
        title = "Analise Tendências",
        description = "Veja gráficos e estatísticas de desempenho",
        bgRes = R.drawable.bg_onb_analise
    ),
    OnboardingPage(
        title = "Comece Agora",
        description = "Você está pronto para otimizar seus ganhos!",
        bgRes = R.drawable.bg_onb_mapafinal
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // HorizontalPager com backgrounds individuais por slide
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }

        // Indicadores de página Hexagonais (Sobrepostos ao Pager)
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

        // Botões de Navegação Pílula
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lógica de botões conforme solicitado
            if (pagerState.currentPage == 0) {
                // Slide 1: Apenas botão "Próximo" centralizado de ponta a ponta
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF06B6D4), Color(0xFF10B981))
                            )
                        ),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Text("Próximo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else if (pagerState.currentPage < onboardingPages.size - 1) {
                // Slides 2 ao 6: Voltar + Próximo
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Voltar", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF06B6D4), Color(0xFF10B981))
                            )
                        ),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Text("Próximo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Slide 7: Voltar + Começar
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Voltar", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("minha_rota_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("is_first_open", false).apply()
                        onNavigateToLogin()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Text("Começar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun HexagonIndicator(isSelected: Boolean) {
    val size = if (isSelected) 14.dp else 10.dp
    val color = if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f)
    
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val radius = this.size.width / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val path = Path()
                
                for (i in 0..5) {
                    val angle = Math.toRadians((60 * i - 30).toDouble())
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()
                    
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                
                if (isSelected) {
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.3f),
                        style = Stroke(width = 8f)
                    )
                    drawPath(
                        path = path,
                        color = color
                    )
                } else {
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2f)
                    )
                }
            }
    )
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Individual por Slide (Imersão Total Edge-to-Edge)
        Image(
            painter = painterResource(id = page.bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Camada de Conteúdo (Tipografia)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp), // Espaço para indicadores e botões
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE5E5EA),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
