package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun CardTurno(
    horasTrabalhadas: String,
    ganhoBruto: Double,
    custoRua: Double,
    ganhoLiquido: Double,
    valorPorHora: Double = 0.0
) {
    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Resumo do Turno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "R$ ${String.format("%.2f", ganhoLiquido)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = VerdeNeon
            )
            
            Text(
                text = "$horasTrabalhadas trabalhadas",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResumoItem(
                    label = "Bruto",
                    valor = "R$ ${String.format("%.2f", ganhoBruto)}",
                    cor = Color.White.copy(alpha = 0.9f)
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                
                ResumoItem(
                    label = "Custos",
                    valor = "R$ ${String.format("%.2f", custoRua)}",
                    cor = Color(0xFFE57373)
                )

                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                ResumoItem(
                    label = "R$ / Hora",
                    valor = "R$ ${String.format("%.2f", valorPorHora)}",
                    cor = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun ResumoItem(label: String, valor: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = cor
        )
    }
}
