package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.LoginRequest
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel(private val authManager: AuthManager) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        authManager.saveToken(it.token)
                        onSuccess()
                    }
                } else {
                    errorMessage = "Login failed: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
