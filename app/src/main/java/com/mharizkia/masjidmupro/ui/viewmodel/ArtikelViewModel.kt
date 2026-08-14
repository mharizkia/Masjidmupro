package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.Artikel
import com.mharizkia.masjidmupro.data.model.ReviewRequest
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ArtikelViewModel(private val authManager: AuthManager) : ViewModel() {
    var pendingArtikel by mutableStateOf<List<Artikel>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    var selectedStatus by mutableStateOf("all")

    val filteredArtikel: List<Artikel>
        get() = if (searchQuery.isBlank()) {
            pendingArtikel
        } else {
            pendingArtikel.filter {
                it.judul.contains(searchQuery, ignoreCase = true) ||
                        it.pengguna?.nama?.contains(searchQuery, ignoreCase = true) == true
            }
        }

    fun fetchPendingArtikel() {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getPendingArtikel(
                    token = token,
                    status = if (selectedStatus == "all") null else selectedStatus
                )
                if (response.isSuccessful) {
                    pendingArtikel = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to fetch pending artikel: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun reviewArtikel(artikel: Artikel, action: String, catatan: String? = null, onResult: (Boolean) -> Unit) {
        val token = authManager.getToken() ?: return
        val identifier = artikel.slug ?: artikel.id.toString()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.reviewArtikel(
                    token,
                    identifier,
                    ReviewRequest(action, catatan)
                )
                if (response.isSuccessful) {
                    fetchPendingArtikel()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
