package com.raffastudioproducoes.minharota.ui.screens.perfil

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raffastudioproducoes.minharota.data.local.SharedPreferencesManager
import com.raffastudioproducoes.minharota.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilViewModel : ViewModel() {
    private val _nomeUsuario = MutableStateFlow("")
    val nomeUsuario: StateFlow<String> = _nomeUsuario

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _dataAniversario = MutableStateFlow("")
    val dataAniversario: StateFlow<String> = _dataAniversario

    private val _fotoPerfilUrl = MutableStateFlow("")
    val fotoPerfilUrl: StateFlow<String> = _fotoPerfilUrl

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = UserRepository()

    fun carregarDadosPerfil(context: Context) {
        val prefs = SharedPreferencesManager(context)
        _nomeUsuario.value = prefs.obterNomeUsuario()
        _email.value = prefs.obterEmail()
        _dataAniversario.value = prefs.obterDataAniversario()
        _fotoPerfilUrl.value = prefs.obterFotoPerfilUrl()

        carregarDadosDoServidor(context)
    }

    private fun carregarDadosDoServidor(context: Context) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val isProDoc = doc.getBoolean("isPro") ?: false
                    val nomePlanoDoc = doc.getString("nomePlano")
                        ?: if (isProDoc) "Premium" else "Free" // <--- Adicione isso
                    val dataVencimentoDoc =
                        doc.getString("dataVencimento") ?: "" // <--- Se houver no Firestore
                    val nome = doc.getString("name") ?: ""
                    val emailDoc = doc.getString("email") ?: ""
                    val aniversario = doc.getString("dataAniversario") ?: ""
                    val foto = doc.getString("photoUrl") ?: ""

                    if (nome.isNotBlank()) _nomeUsuario.value = nome
                    if (emailDoc.isNotBlank()) _email.value = emailDoc
                    if (aniversario.isNotBlank()) _dataAniversario.value = aniversario
                    if (foto.isNotBlank()) _fotoPerfilUrl.value = foto

                    val prefs = SharedPreferencesManager(context)
                    prefs.salvarIsPro(isProDoc)
                    prefs.salvarNomePlano(nomePlanoDoc)
                    if (dataVencimentoDoc.isNotBlank()) {
                        prefs.salvarDataVencimento(dataVencimentoDoc)
                    }
                    if (nome.isNotBlank()) prefs.salvarNomeUsuario(nome)
                    if (emailDoc.isNotBlank()) prefs.salvarEmail(emailDoc)
                    if (aniversario.isNotBlank()) prefs.salvarDataAniversario(aniversario)
                    if (foto.isNotBlank()) prefs.salvarFotoPerfilUrl(foto)
                }
            }
    }

    fun atualizarNomeUsuario(nome: String, context: Context) {
        _nomeUsuario.value = nome
        SharedPreferencesManager(context).salvarNomeUsuario(nome)

        auth.currentUser?.uid?.let { uid ->
            repository.updateUserField(uid, mapOf("name" to nome)) { _ -> }
        }
    }

    fun atualizarEmail(novoEmail: String, context: Context) {
        if (novoEmail.isBlank()) return
        _email.value = novoEmail
        SharedPreferencesManager(context).salvarEmail(novoEmail)

        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            repository.updateUserField(uid, mapOf("email" to novoEmail)) { _ -> }
        }

        currentUser?.updateEmail(novoEmail)?.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                // Tratar erro de login recente se necessário
            }
        }
    }

    fun atualizarDataAniversario(data: String, context: Context) {
        _dataAniversario.value = data
        SharedPreferencesManager(context).salvarDataAniversario(data)

        auth.currentUser?.uid?.let { uid ->
            repository.updateUserField(uid, mapOf("dataAniversario" to data)) { _ -> }
        }
    }

    fun atualizarFotoPerfilUrl(url: String, context: Context) {
        _fotoPerfilUrl.value = url
        SharedPreferencesManager(context).salvarFotoPerfilUrl(url)

        auth.currentUser?.uid?.let { uid ->
            repository.updateUserField(uid, mapOf("photoUrl" to url)) { _ -> }
        }
    }
}