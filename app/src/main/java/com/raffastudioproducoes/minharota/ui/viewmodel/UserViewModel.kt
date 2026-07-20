package com.raffastudioproducoes.minharota.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.raffastudioproducoes.minharota.domain.model.User
import com.raffastudioproducoes.minharota.repository.UserRepository

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    val userData = mutableStateOf<User?>(null)

    fun registerOrUpdateUser(user: User, onComplete: () -> Unit) {
        repository.saveUser(user) { success ->
            loadUserData(user.uid)
            onComplete() //Só chama quando o Fiestone terminar
        }
    }

    fun loadUserData(uid: String) {
        repository.getUser(uid) { user ->
            userData.value = user
        }
    }
}