package com.raffastudioproducoes.minharota.ui.screens.help

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@Composable
fun HelpScreen(
    onClose: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E22)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // --- CABEÇALHO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "?",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "AJUDA & FAQ",
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar",
                    tint = textColor.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- ABAS SUPERIORES ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton("📖 Tutorial", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
            TabButton("💬 FAQ", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
            TabButton("✉️ Contato", selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> TutorialTabContent()
            1 -> FaqTabContent()
            2 -> ContactTabContent()
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF2C2C32) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ==========================================
// ABA 1: TUTORIAL COM DESTAQUES EM BRANCO
// ==========================================
@Composable
fun TutorialTabContent() {
    val tutorials = listOf(
        TutorialItemData(
            title = "🏠 Aba Hoje — Registro do dia",
            subtitle = "Tudo começa aqui. Todo dia que você trabalhar, registre:",
            topics = listOf(
                listOf(
                    HighlightedText("• Horário de início e término", true),
                    HighlightedText(" do turno", false)
                ),
                listOf(
                    HighlightedText("• Se teve ", false),
                    HighlightedText("intervalo/pausa", true),
                    HighlightedText(", marque e informe os horários", false)
                ),
                listOf(
                    HighlightedText("• Digite o ", false),
                    HighlightedText("ganho bruto", true),
                    HighlightedText(" do dia (total que recebeu)", false)
                ),
                listOf(
                    HighlightedText("• Adicione os ", false),
                    HighlightedText("custos do dia", true),
                    HighlightedText(" (gasolina, lanche, etc.) clicando no +", false)
                ),
                listOf(
                    HighlightedText("• Veja o ", false),
                    HighlightedText("líquido", true),
                    HighlightedText(" calculado automaticamente (bruto - custos)", false)
                ),
                listOf(
                    HighlightedText("• Clique em ", false),
                    HighlightedText("\"Salvar dia\"", true),
                    HighlightedText(" para registrar e distribuir nas caixinhas", false)
                )
            ),
            tip = "💡 Você pode registrar datas passadas alterando a data no topo do formulário."
        ),
        TutorialItemData(
            title = "📦 Aba Caixas — Organização financeira",
            subtitle = "As caixinhas são \"envelopes virtuais\" que dividem seu dinheiro automaticamente:",
            topics = listOf(
                listOf(
                    HighlightedText("• Cada caixinha recebe uma ", false),
                    HighlightedText("porcentagem", true),
                    HighlightedText(", do seu líquido diário", false)
                ),
                listOf(
                    HighlightedText("• Após salvar o dia, vá em Caixas e clique ", false),
                    HighlightedText("\"Confirmar depósito\"", true),
                    HighlightedText(", em cada uma", false)
                ),
                listOf(
                    HighlightedText("• Acompanhe os depósitos de ", false),
                    HighlightedText("hoje, semana, mês e ano", true),
                    HighlightedText(" nas abas", false)
                ),
                listOf(
                    HighlightedText("• Defina uma ", false),
                    HighlightedText("meta (R$)", true),
                    HighlightedText(" para cada caixinha e acompanhe o progresso", false)
                ),
                listOf(
                    HighlightedText("• Você pode ", false),
                    HighlightedText("pausar", true),
                    HighlightedText(" uma caixinha temporariamente", false)
                ),
                listOf(
                    HighlightedText("• O percentual total de ser ", false),
                    HighlightedText("100%", true),
                    HighlightedText(" - o app avisa se estiver errado", false)
                )
            ),
            tip = "💡 O total alocado nas caixinhas deve somar 100% para melhor aproveitamento."
        ),
        TutorialItemData(
            title = "📄 Aba Contas — Controle de despesas fixas",
            subtitle = "Cadastre suas contas fixas e parceladas para calcular a meta diária automática:",
            topics = listOf(
                listOf(
                    HighlightedText(
                        "• Adicione contas como: moto, aluguel, seguro, celular, etc",
                        false
                    )
                ),
                listOf(
                    HighlightedText("• Informe o ", false),
                    HighlightedText("valor da parcela", true),
                    HighlightedText(", e o", false),
                    HighlightedText(", vencimento", true)
                ),
                listOf(
                    HighlightedText("• Para perceladas, marque ", false),
                    HighlightedText("\"É parcelado\"", true),
                    HighlightedText(" e informe as parcelas restantes", false)
                ),
                listOf(
                    HighlightedText("• O app calcula a ", false),
                    HighlightedText("meta diária automatica", true),
                    HighlightedText(" para você cobrir tudo", false)
                ),
                listOf(
                    HighlightedText("• Quando pagar uma conta, clique em ", false),
                    HighlightedText("\"Pagar\"", true),
                    HighlightedText(" - ela vai para \"Contas pagas\"", false)
                )
            ),
            tip = "💡 A meta diária leva em conta sues dias de folga (configurados em Caixas)"
        ),
        TutorialItemData(
            title = "🗂️ Aba Dívidas — Controle de dívidas",
            subtitle = "Para dívidas maiores (empréstimos, cartão, etc.) que não são contas mensais fixas:",
            topics = listOf(
                listOf(
                    HighlightedText("• Cadastre com o ", false),
                    HighlightedText("valor total ", true),
                    HighlightedText(", e o", false),
                    HighlightedText(", valor já pago", false)
                ),
                listOf(
                    HighlightedText(
                        "• Para parceladas, informe parcela, total de parcelas e quantas já pagou",
                        false
                    )
                ),
                listOf(
                    HighlightedText("• O app mostra o ", false),
                    HighlightedText("saldo restante ", true),
                    HighlightedText("e o progresso de quitação", false)
                ),
                listOf(
                    HighlightedText("• Quando quitar, clique em ", false),
                    HighlightedText("\"Quitar\"", true),
                    HighlightedText(" - a dívida vai para o histótico", false)
                )
            ),
            tip = null
        ),
        TutorialItemData(
            title = "📊 Aba Extrato — Visão completa",
            subtitle = "Veja todas as movimentações (entrada, saídas, gastos) num só lugar:",
            topics = listOf(
                listOf(
                    HighlightedText("• Use os ", false),
                    HighlightedText("atalhos rápidos", true),
                    HighlightedText(" (Hoje, Semana, Mês) para filtrar", false)
                ),
                listOf(
                    HighlightedText("• Ou defina um ", false),
                    HighlightedText("período personalizado", true),
                    HighlightedText(" com datas de início e fim", false)
                ),
                listOf(
                    HighlightedText("• O extrato mostra ", false),
                    HighlightedText("saldo, entradas e saídas", true),
                    HighlightedText(" do período selecionado", false)
                )
            ),
            tip = null
        ),
        TutorialItemData(
            title = "📈 Aba Gráficos — Analytics",
            subtitle = "Analise sua performance com diferentes visualizações:",
            topics = listOf(
                listOf(
                    HighlightedText("• Diário: ", true),
                    HighlightedText("ganhos dos últimos 14 dias", false)
                ),
                listOf(
                    HighlightedText("• Barras: ", true),
                    HighlightedText("comparativo bruto vs líquido por dia", false)
                ),
                listOf(
                    HighlightedText("• Quinzena/Mês: ", true),
                    HighlightedText("evolução por período", false)
                ),
                listOf(
                    HighlightedText("• Pizza: ", true),
                    HighlightedText("distribuição entre caixinhas", false)
                ),
                listOf(
                    HighlightedText("• Horários de Ouro 🎖️: ", true),
                    HighlightedText(
                        "descubra quais dias e horários você ganha mais por hora",
                        false
                    )
                )
            ),
            tip = "💡 Horários de Ouro requer ao menos 5 registros com horários preenchidos."
        ),
        TutorialItemData(
            title = "👤 Perfis de usuários",
            subtitle = "O app suporta múltiplos perfis - ideal para quem divide o celular:",
            topics = listOf(
                listOf(
                    HighlightedText(
                        "• Acessa pelo botão com seu nome no canto superios direito",
                        false
                    )
                ),
                listOf(
                    HighlightedText("• Cada perfil tem seus dados ", false),
                    HighlightedText("completamente separados", true)
                ),
                listOf(
                    HighlightedText(
                        "• Você pode criar, trocar e remover perfis a qualquer momento",
                        false
                    )
                )
            ),
            tip = null
        ),
        TutorialItemData(
            title = "📦 Backup e restauração",
            subtitle = "Para não peder seus dados, faça backup regularmente:",
            topics = listOf(
                listOf(
                    HighlightedText("• Vá no menu (≡) - ", false),
                    HighlightedText("Backup/Restaurar", true)
                ),
                listOf(
                    HighlightedText("• Exporte um arquivo ", false),
                    HighlightedText(".Json", true),
                    HighlightedText(
                        " e guarde em local seguro (Drive, WhatsApp para si mesmo, etc.)",
                        false
                    )
                ),
                listOf(
                    HighlightedText(
                        "• Para restaurar, abra o app, vá em Backup e importe o arquivo",
                        false
                    )
                )
            ),
            tip = "⚠️ Os dados ficam no navegador/app. se limpar o cache perde tudo. Faça backup frequente!"
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tutorials.size) { index ->
            val item = tutorials[index]
            var expanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = item.subtitle,
                                color = Color.Gray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            if (item.topics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                item.topics.forEach { topicParts ->
                                    Text(
                                        text = buildAnnotatedString {
                                            topicParts.forEach { part ->
                                                withStyle(
                                                    style = SpanStyle(
                                                        fontWeight = if (part.isBold) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (part.isBold) Color.White else Color.Gray
                                                    )
                                                ) {
                                                    append(part.text)
                                                }
                                            }
                                        },
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }

                            item.tip?.let { tip ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF25252B))
                                        .height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .background(VerdeNeon)
                                    )
                                    Text(
                                        text = tip,
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class HighlightedText(val text: String, val isBold: Boolean)
data class TutorialItemData(
    val title: String,
    val subtitle: String,
    val topics: List<List<HighlightedText>>,
    val tip: String?
)

// ==========================================
// ABA 2: FAQ
// ==========================================
@Composable
fun FaqTabContent() {
    val faqs = listOf(
        Pair(
            "Perdi meus dados, o que faço?",
            "Os dados ficam salvos no armazenamento local do dispositivo. Se você limpou o cache ou desinstalou o app, os dados podem ter sido perdidos. Por isso, faça backup regularmente pelo menu."
        ),
        Pair(
            "Por que o total alocado nas caixinhas não é 100%?",
            "Você pode ajustar os percentuais de acordo com sua prioridade, mas o ideal é que a soma atinja 100% para distribuir todo o lucro líquido gerado."
        ),
        Pair(
            "Como funciona a meta diária automática?",
            "O app calcula com base nas suas contas fixas pendentes e dias úteis restantes até o vencimento."
        ),
        Pair(
            "Posso registrar dias passados?",
            "Sim! Basta alterar a data no topo do formulário na aba Hoje."
        ),
        Pair(
            "O app funciona sem internet?",
            "Sim, seus dados locais permitem o funcionamento offline."
        ),
        Pair(
            "Como instalar o app no celular?",
            "Você pode baixar e instalar o aplicativo diretamente pela Google Play Store no seu dispositivo Android."
        ),
        Pair(
            "Posso usar em mais de um celular?",
            "Sim, basta fazer login com a sua conta cadastrada em outros dispositivos para acessar suas informações."
        ),
        Pair(
            "O que é \"Horários de ouro\"?",
            "É um recurso exclusivo dos planos avançados que indica os períodos e mapas de calor com maior lucratividade para rodar na rua."
        ),
        Pair(
            "Caixinha pausada perde os dados?",
            "Não. Ao pausar uma caixinha, todo o saldo atual e o progresso acumulado continuam salvos e seguros."
        ),
        Pair(
            "Como o aplicativo calcula o ganho líquido?",
            "O ganho líquido é calculado automaticamente subtraindo todos os custos cadastrados do dia (como combustível e alimentação) do seu ganho bruto total."
        ),
        Pair(
            "Como funcionam os relatórios de Inteligência Artificial?",
            "Disponível para usuários Pro, a IA analisa seus dados de turnos, contas e veículos para gerar insights diários com dicas práticas e motivacionais."
        ),
        Pair(
            "Como posso acompanhar meu limite do MEI?",
            "Na aba Contas, o aplicativo monitora automaticamente o seu faturamento anual com base nos turnos registrados e avisa se você está próximo do teto limite."
        ),
        Pair(
            "Como faço para adicionar um custo no turno?",
            "Na aba Hoje, basta clicar no botão de mais (+) ao lado dos custos para adicionar gastos com gasolina, lanche ou manutenção rápida do dia."
        ),
        Pair(
            "O que acontece quando atinjo o limite de caixinhas no plano Free?",
            "O aplicativo permite gerenciar até 3 caixinhas gratuitamente. Ao tentar adicionar mais, você será direcionado para conhecer os planos superiores."
        ),
        Pair(
            "Como altero os dias de folga fixos?",
            "Você pode configurar seus dias de folga na aba Caixas, onde os dias marcados são descontados automaticamente do cálculo da meta diária."
        ),
        Pair(
            "Posso editar ou excluir um turno que salvei errado?",
            "Sim, pelo histórico na aba Extrato você pode gerenciar, editar ou excluir turnos anteriores salvos no aplicativo."
        ),
        Pair(
            "Como faço para restaurar meus dados após trocar de celular?",
            "Como os dados são salvos na nuvem pelo Firebase quando você faz login na sua conta, basta entrar com seu e-mail e senha no novo aparelho."
        ),
        Pair(
            "Qual a diferença entre os planos Premium e Pro?",
            "O plano Premium libera recursos como histórico ilimitado e OCR de documentos, enquanto o Pro inclui gestão completa de múltiplos motoristas, relatórios avançados e API."
        ),
        Pair(
            "Como funciona o OCR de documentos?",
            "É um recurso que permite ler e digitalizar informações rapidamente através da câmera do celular para facilitar seus registros financeiros."
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(faqs.size) { index ->
            val faq = faqs[index]
            var expanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.first,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Text(
                            text = faq.second,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ABA 3: CONTATO
// ==========================================
@Composable
fun ContactTabContent() {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current // Contexto necessário para abrir os links

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✉️ Entre em contato",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Tem dúvidas, sugestões ou encontrou algum problema? Fale com a gente:",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- E-MAIL (Abre o Gmail com assunto preenchido) ---
                        ContactOptionRow("E-mail", "Enviar e-mail") {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data =
                                    Uri.parse("mailto:contato.raffasp@gmail.com") // Substitua pelo seu e-mail de suporte
                                putExtra(Intent.EXTRA_SUBJECT, "Suporte e Ajuda")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Caso o usuário não tenha app de e-mail padrão
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

                        // --- WHATSAPP (Abre conversa direta para o número) ---
                        ContactOptionRow("WhatsApp / Suporte", "Abrir conversa") {
                            val numeroWhatsApp =
                                "5521998887674" // Coloque seu DDI + DDD + Número aqui (Ex: 55 + 11 + 9XXXXXXXX)
                            val mensagem = "Olá, preciso de ajuda com o aplicativo Minha Rota."
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/$numeroWhatsApp?text=${Uri.encode(mensagem)}")
                            )
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Caso não tenha o WhatsApp instalado
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

                        // --- INSTAGRAM (Abre o perfil) ---
                        ContactOptionRow("Instagram", "Visitar perfil") {
                            val urlInstagram =
                                "https://instagram.com/rafaelmachadogalvao" // Substitua pelo link do seu perfil do Instagram
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlInstagram))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Tratamento de erro caso falhe
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF25252B)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(VerdeNeon)
                            )
                            Text(
                                text = "💡 Ao entrar em contato, informe o que estava tentando fazer e qual erro ou dúvida apareceu. Isso agiliza muito o atendimento!",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactOptionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text(text = subtitle, color = Color.Gray, fontSize = 13.sp)
    }
}