package com.raffastudioproducoes.minharota.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.domain.model.Corrida
import com.raffastudioproducoes.minharota.domain.model.TemporaryRide
import java.util.*

class RideAssistantService : AccessibilityService() {

    private var currentPendingRide: TemporaryRide? = null
    private lateinit var prefs: SharedPreferencesManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = SharedPreferencesManager(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        val rootNode = rootInActiveWindow ?: return

        when (packageName) {
            "com.ifood.driver" -> handleIFood(rootNode)
            "com.ubercab.driver" -> handleUber(rootNode)
            "com.taxis99" -> handle99(rootNode)
            "com.zedelivery.entregador" -> handleZeDelivery(rootNode)
        }
    }

    private fun handleIFood(root: AccessibilityNodeInfo) {
        // Oferta: "R$" + "Entregar" ou "Coletar"
        val nodes = findTextNodes(root, listOf("R$", "Entregar", "Coletar"))
        if (nodes.any { it.text?.contains("R$") == true } && 
            (nodes.any { it.text?.contains("Entregar") == true } || nodes.any { it.text?.contains("Coletar") == true })) {
            
            val valor = extractValue(nodes)
            val km = extractKm(nodes)
            
            if (valor > 0) {
                currentPendingRide = TemporaryRide(
                    packageName = "com.ifood.driver",
                    estimatedValue = valor,
                    estimatedKm = km,
                    pingTime = System.currentTimeMillis()
                )
            }
        }

        // Conclusão: "Rota Concluída" ou "Entrega Finalizada"
        if (findTextNodes(root, listOf("Rota Concluída", "Entrega Finalizada", "Cheguei", "Finalizar")).isNotEmpty()) {
            consolidateRide()
        }
    }

    private fun handleUber(root: AccessibilityNodeInfo) {
        // Oferta: "R$" e "km" em card flutuante
        val nodes = findTextNodes(root, listOf("R$", "km"))
        if (nodes.any { it.text?.contains("R$") == true } && nodes.any { it.text?.contains("km") == true }) {
            val valor = extractValue(nodes)
            val km = extractKm(nodes)
            
            if (valor > 0) {
                currentPendingRide = TemporaryRide(
                    packageName = "com.ubercab.driver",
                    estimatedValue = valor,
                    estimatedKm = km,
                    pingTime = System.currentTimeMillis()
                )
            }
        }

        // Conclusão: "Você recebeu R$"
        if (findTextNodes(root, listOf("Você recebeu R$", "Recibo")).isNotEmpty()) {
            consolidateRide()
        }
    }

    private fun handle99(root: AccessibilityNodeInfo) {
        // Oferta: "R$" e "km"
        val nodes = findTextNodes(root, listOf("R$", "km"))
        if (nodes.any { it.text?.contains("R$") == true } && nodes.any { it.text?.contains("km") == true }) {
            val valor = extractValue(nodes)
            val km = extractKm(nodes)
            
            if (valor > 0) {
                currentPendingRide = TemporaryRide(
                    packageName = "com.taxis99",
                    estimatedValue = valor,
                    estimatedKm = km,
                    pingTime = System.currentTimeMillis()
                )
            }
        }

        // Conclusão: "Ganho da viagem" ou "Corrida finalizada"
        if (findTextNodes(root, listOf("Ganho da viagem", "Corrida finalizada")).isNotEmpty()) {
            consolidateRide()
        }
    }

    private fun handleZeDelivery(root: AccessibilityNodeInfo) {
        // Oferta: "R$" e "Aceitar Entrega"
        val nodes = findTextNodes(root, listOf("R$", "Aceitar Entrega"))
        if (nodes.any { it.text?.contains("R$") == true } && nodes.any { it.text?.contains("Aceitar Entrega") == true }) {
            val valor = extractValue(nodes)
            val km = extractKm(nodes)
            
            if (valor > 0) {
                currentPendingRide = TemporaryRide(
                    packageName = "com.zedelivery.entregador",
                    estimatedValue = valor,
                    estimatedKm = km,
                    pingTime = System.currentTimeMillis()
                )
            }
        }

        // Conclusão: "Entrega realizada com sucesso"
        if (findTextNodes(root, listOf("Entrega realizada com sucesso", "Assinatura")).isNotEmpty()) {
            consolidateRide()
        }
    }

    private fun consolidateRide() {
        currentPendingRide?.let { temp ->
            val appName = when(temp.packageName) {
                "com.ifood.driver" -> "iFood"
                "com.ubercab.driver" -> "Uber"
                "com.taxis99" -> "99"
                "com.zedelivery.entregador" -> "Zé Delivery"
                else -> "App"
            }

            val novaCorrida = Corrida(
                id = UUID.randomUUID().toString(),
                valor = temp.estimatedValue,
                timestamp = System.currentTimeMillis(),
                km = temp.estimatedKm,
                app = appName
            )

            // Persistir no turno atual se houver
            val turnos = prefs.obterTurnos().toMutableList()
            if (turnos.isNotEmpty()) {
                val ultimoTurno = turnos.last()
                // Verificar se o turno é de hoje
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val hoje = sdf.format(Date())
                
                if (ultimoTurno.data == hoje) {
                    val corridasAtualizadas = ultimoTurno.corridas.toMutableList()
                    corridasAtualizadas.add(novaCorrida)
                    
                    val turnoAtualizado = ultimoTurno.copy(
                        corridas = corridasAtualizadas,
                        ganhoBruto = ultimoTurno.ganhoBruto + novaCorrida.valor,
                        ganhoLiquido = ultimoTurno.ganhoLiquido + novaCorrida.valor // Simplificado
                    )
                    
                    turnos[turnos.size - 1] = turnoAtualizado
                    prefs.salvarTurnos(turnos)
                }
            }
            
            currentPendingRide = null
        }
    }

    private fun findTextNodes(node: AccessibilityNodeInfo?, keywords: List<String>): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        if (node == null) return result

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString() ?: ""
            
            if (keywords.any { text.contains(it, ignoreCase = true) }) {
                result.add(child)
            }
            
            result.addAll(findTextNodes(child, keywords))
        }
        return result
    }

    private fun extractValue(nodes: List<AccessibilityNodeInfo>): Double {
        nodes.forEach { node ->
            val text = node.text?.toString() ?: ""
            if (text.contains("R$")) {
                val valueStr = text.replace("R$", "").replace(",", ".").trim()
                return valueStr.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }

    private fun extractKm(nodes: List<AccessibilityNodeInfo>): Double {
        nodes.forEach { node ->
            val text = node.text?.toString() ?: ""
            if (text.contains("km", ignoreCase = true)) {
                val kmStr = text.replace("km", "", ignoreCase = true).replace(",", ".").trim()
                return kmStr.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }

    override fun onInterrupt() {}
}
