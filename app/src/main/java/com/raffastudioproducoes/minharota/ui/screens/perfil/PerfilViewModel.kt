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

    private val _cpf = MutableStateFlow("")
    val cpf: StateFlow<String> = _cpf

    private val _dataAniversario = MutableStateFlow("")
    val dataAniversario: StateFlow<String> = _dataAniversario

    private val _fotoPerfilUrl = MutableStateFlow("")
    val fotoPerfilUrl: StateFlow<String> = _fotoPerfilUrl

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = UserRepository()

    fun carregarDadosPerfil(context: Context) {
        val prefs = SharedPreferencesManager(context)
        val firebaseUser = auth.currentUser

        _nomeUsuario.value = prefs.obterNomeUsuario()
        _email.value = prefs.obterEmail()
        _dataAniversario.value = prefs.obterDataAniversario()
        _fotoPerfilUrl.value = prefs.obterFotoPerfilUrl()

        aplicarFallbackFirebase(firebaseUser, prefs)

        // Força atualização do perfil do Firebase (displayName pode ter sido setado após o login em cache)
        firebaseUser?.reload()?.addOnCompleteListener {
            aplicarFallbackFirebase(auth.currentUser, prefs)
        }

        carregarDadosDoServidor(context)
    }

    private fun aplicarFallbackFirebase(
        firebaseUser: com.google.firebase.auth.FirebaseUser?,
        prefs: SharedPreferencesManager
    ) {
        if (_email.value.isEmpty() && !firebaseUser?.email.isNullOrBlank()) {
            _email.value = firebaseUser?.email ?: ""
            prefs.salvarEmail(firebaseUser?.email ?: "")
        }

        if (_nomeUsuario.value.isEmpty() && !firebaseUser?.displayName.isNullOrBlank()) {
            _nomeUsuario.value = firebaseUser?.displayName ?: ""
            prefs.salvarNomeUsuario(firebaseUser?.displayName ?: "")
        }
    }

    private fun carregarDadosDoServidor(context: Context) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nome = doc.getString("name") ?: ""
                    val emailDoc = doc.getString("email") ?: ""
                    val cpfDoc = doc.getString("cpf") ?: ""
                    val aniversario = doc.getString("dataAniversario") ?: ""
                    val foto = doc.getString("photoUrl") ?: ""

                    if (nome.isNotBlank()) _nomeUsuario.value = nome
                    if (emailDoc.isNotBlank()) _email.value = emailDoc
                    if (cpfDoc.isNotBlank()) _cpf.value = cpfDoc
                    if (aniversario.isNotBlank()) _dataAniversario.value = aniversario
                    if (foto.isNotBlank()) _fotoPerfilUrl.value = foto

                    val prefs = SharedPreferencesManager(context)
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

    fun atualizarCpf(novoCpf: String, context: Context) {
        _cpf.value = novoCpf

        auth.currentUser?.uid?.let { uid ->
            repository.updateUserField(uid, mapOf("cpf" to novoCpf)) { _ -> }
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

    fun excluirConta(context: Context, onComplete: () -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            // Remove do Firestore
            firestore.collection("usuarios").document(uid).delete().addOnCompleteListener {
                // Remove do Auth e desloga
                currentUser.delete().addOnCompleteListener {
                    auth.signOut()
                    onComplete()
                }
            }
        } else {
            auth.signOut()
            onComplete()
        }
    }
}
