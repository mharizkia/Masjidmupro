package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.Berita
import com.mharizkia.masjidmupro.data.model.ReviewRequest
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class BeritaViewModel(private val authManager: AuthManager) : ViewModel() {
    var pendingBerita by mutableStateOf<List<Berita>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    var selectedStatus by mutableStateOf("all")

    val filteredBerita: List<Berita>
        get() = if (searchQuery.isBlank()) {
            pendingBerita
        } else {
            pendingBerita.filter {
                it.judul.contains(searchQuery, ignoreCase = true)
            }
        }

    fun fetchPendingBerita() {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getPendingBerita(
                    token = token,
                    status = if (selectedStatus == "all") null else selectedStatus
                )
                if (response.isSuccessful) {
                    pendingBerita = response.body() ?: emptyList()
                } else {
                    errorMessage = "Gagal memuat berita: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun reviewBerita(berita: Berita, action: String, catatan: String? = null, onResult: (Boolean) -> Unit) {
        val token = authManager.getToken() ?: return
        val identifier = berita.slug ?: berita.id.toString()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.reviewBerita(
                    token,
                    identifier,
                    ReviewRequest(action, catatan)
                )
                if (response.isSuccessful) {
                    fetchPendingBerita()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun storeBerita(
        judul: String,
        deskripsi: String,
        hari: String,
        tanggal: String,
        jam: String,
        penulis: String?,
        onResult: (Boolean) -> Unit
    ) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = mapOf(
                    "judul" to judul,
                    "deskripsi" to deskripsi,
                    "hari" to hari,
                    "tanggal" to tanggal,
                    "jam" to jam,
                    "nama_pembuat_artikel" to penulis
                )
                val response = RetrofitClient.instance.storeBerita(token, request)
                if (response.isSuccessful) {
                    fetchPendingBerita()
                    onResult(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = "Gagal menyimpan berita: ${response.code()} $errorBody"
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
