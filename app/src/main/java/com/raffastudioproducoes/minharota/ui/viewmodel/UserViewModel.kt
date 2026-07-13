package com.raffastudioproducoes.minharota.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.domain.model.User
import com.raffastudioproducoes.minharota.repository.UserRepository

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    // Variável que a tela observa
    val userData = mutableStateOf<User?>(null)

    fun loadUserData(uid: String) {
        repository.getUser(uid) { user ->
            userData.value = user
        }
    }
}