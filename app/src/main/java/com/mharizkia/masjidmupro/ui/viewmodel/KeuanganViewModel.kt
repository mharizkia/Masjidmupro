package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.Keuangan
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class KeuanganViewModel(private val authManager: AuthManager) : ViewModel() {
    var keuanganList by mutableStateOf<List<Keuangan>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Filter states
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    var selectedJenis by mutableStateOf("all")

    // Derived list of potential parents (transactions that don't have a parent)
    val parentOptions: List<Keuangan>
        get() = keuanganList.filter { it.profilMasjidId != null } // Simplified for now, showing all as options

    fun fetchKeuangan() {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getKeuangan(
                    token = token,
                    startDate = if (startDate.isBlank()) null else startDate,
                    endDate = if (endDate.isBlank()) null else endDate,
                    jenisBiaya = if (selectedJenis == "all") null else selectedJenis
                )
                if (response.isSuccessful) {
                    keuanganList = response.body() ?: emptyList()
                } else {
                    errorMessage = "Gagal memuat keuangan: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun storeKeuangan(
        judul: String,
        tanggal: String,
        jumlah: Double?,
        jenis: String,
        parentId: Int? = null,
        onResult: (Boolean) -> Unit
    ) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val request = mutableMapOf<String, Any?>(
                    "judul_kegiatan" to judul,
                    "tanggal_kegiatan" to tanggal,
                    "jenis_biaya" to jenis
                )
                if (jumlah != null) request["jumlah_biaya"] = jumlah
                if (parentId != null) request["parent_id"] = parentId

                val response = RetrofitClient.instance.storeKeuangan(token, request)
                if (response.isSuccessful) {
                    fetchKeuangan()
                    onResult(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = "Gagal menyimpan: ${response.code()} $errorBody"
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
