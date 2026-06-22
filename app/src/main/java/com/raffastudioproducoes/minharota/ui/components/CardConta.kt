package com.raffastudioproducoes.minharota.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CardConta(
    nome: String,
    dataVencimento: String,
    valor: Double,
    pago: Boolean,
    onTogglePago: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E20)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pago) Color.Gray else Color.White
                )
                Text(
                    text = "Vence em: $dataVencimento",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "R$ ${String.format("%.2f", valor)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (pago) Color.Gray else Color(0xFFEF4444)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Excluir", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }

                IconButton(onClick = onTogglePago) {
                    Icon(
                        imageVector = if (pago) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (pago) "Paga" else "Pendente",
                        tint = if (pago) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
