package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.Masjid
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class ProfilViewModel(private val authManager: AuthManager) : ViewModel() {
    var masjidData by mutableStateOf<Masjid?>(null)
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchProfil() {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getProfil(token)
                if (response.isSuccessful) {
                    masjidData = response.body()
                } else {
                    errorMessage = "Gagal mengambil profil: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfil(
        namaMasjid: String,
        alamat: String,
        gmaps: String?,
        instagram: String?,
        youtube: String?,
        facebook: String?,
        tiktok: String?,
        donasi: String?,
        visiMisi: String?,
        sejarah: String?
    ) {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            isSuccess = false
            try {
                val data = mapOf(
                    "nama_masjid" to namaMasjid,
                    "alamat_lengkap" to alamat,
                    "link_gmaps" to gmaps,
                    "link_instagram" to instagram,
                    "link_youtube" to youtube,
                    "link_facebook" to facebook,
                    "link_tiktok" to tiktok,
                    "link_donasi" to donasi,
                    "konten_visi_misi" to visiMisi,
                    "konten_sejarah" to sejarah
                )
                val response = RetrofitClient.instance.updateProfil(token, data)
                if (response.isSuccessful) {
                    masjidData = response.body()
                    isSuccess = true
                } else {
                    errorMessage = "Update gagal: ${response.code()} ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
