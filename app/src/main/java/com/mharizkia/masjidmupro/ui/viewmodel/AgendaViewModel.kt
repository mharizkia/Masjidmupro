package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.Agenda
import com.mharizkia.masjidmupro.data.model.AgendaRequest
import com.mharizkia.masjidmupro.data.model.Penceramah
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AgendaViewModel(private val authManager: AuthManager) : ViewModel() {
    var agendaList by mutableStateOf<List<Agenda>>(emptyList())
    var penceramahList by mutableStateOf<List<Penceramah>>(emptyList())
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    var selectedStatus by mutableStateOf("all")

    fun fetchAgendas(page: Int = 1) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.listAgenda(
                    token = token,
                    status = if (selectedStatus == "all") null else selectedStatus,
                    page = page
                )
                if (response.isSuccessful) {
                    agendaList = response.body()?.data ?: emptyList()
                } else {
                    errorMessage = "Gagal memuat agenda: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchPenceramah(search: String? = null) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.listPenceramah(token, search)
                if (response.isSuccessful) {
                    penceramahList = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                // Ignore error for penceramah list
            }
        }
    }

    fun storeAgenda(request: AgendaRequest, onResult: (Boolean) -> Unit) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            isSuccess = false
            try {
                val response = RetrofitClient.instance.storeAgenda(token, request)
                if (response.isSuccessful) {
                    isSuccess = true
                    fetchAgendas()
                    onResult(true)
                } else {
                    errorMessage = "Gagal membuat agenda: ${response.code()} ${response.errorBody()?.string()}"
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
