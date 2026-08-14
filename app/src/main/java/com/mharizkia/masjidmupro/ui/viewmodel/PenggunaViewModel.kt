package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.PaginatedUser
import com.mharizkia.masjidmupro.data.model.User
import com.mharizkia.masjidmupro.data.model.UserRequest
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class PenggunaViewModel(private val authManager: AuthManager) : ViewModel() {
    var users by mutableStateOf<List<User>>(emptyList())
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")

    fun fetchUsers(page: Int = 1) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.listPengguna(
                    token = token,
                    page = page,
                    search = if (searchQuery.isNotBlank()) searchQuery else null
                )
                if (response.isSuccessful) {
                    users = response.body()?.data ?: emptyList()
                } else {
                    errorMessage = "Gagal memuat pengguna: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        fetchUsers()
    }

    fun deleteUser(id: Int) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.destroyPengguna(token, id)
                if (response.isSuccessful) {
                    fetchUsers()
                } else {
                    errorMessage = "Gagal menghapus pengguna"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    fun saveUser(id: Int?, request: UserRequest, onResult: (Boolean) -> Unit) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val response = if (id == null) {
                    RetrofitClient.instance.storePengguna(token, request)
                } else {
                    RetrofitClient.instance.updatePengguna(token, id, request)
                }
                
                if (response.isSuccessful) {
                    fetchUsers()
                    onResult(true)
                } else {
                    errorMessage = "Gagal menyimpan: ${response.message()}"
                    onResult(false)
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                onResult(false)
            } finally {
                isLoading = false
            }
        }
    }
}
