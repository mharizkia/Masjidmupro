package com.mharizkia.masjidmupro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.data.model.DashboardResponse
import com.mharizkia.masjidmupro.data.model.ArtikelStatusCounts
import com.mharizkia.masjidmupro.data.model.KeuanganChartResponse
import com.mharizkia.masjidmupro.data.remote.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DashboardViewModel(private val authManager: AuthManager) : ViewModel() {
    var dashboardData by mutableStateOf<DashboardResponse?>(null)
    var statusCounts by mutableStateOf<ArtikelStatusCounts?>(null)
    var keuanganChartData by mutableStateOf<KeuanganChartResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchDashboard() {
        val token = authManager.getToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val dashboardDeferred = async { RetrofitClient.instance.getDashboard(token) }
                val countsDeferred = async { RetrofitClient.instance.getArtikelStatusCounts(token) }
                val chartDeferred = async { RetrofitClient.instance.getKeuanganChart(token) }

                val dashboardResponse = dashboardDeferred.await()
                val countsResponse = countsDeferred.await()
                val chartResponse = chartDeferred.await()

                if (dashboardResponse.isSuccessful) {
                    dashboardData = dashboardResponse.body()
                } else {
                    errorMessage = "Gagal memuat dashboard: ${dashboardResponse.code()}"
                }

                if (countsResponse.isSuccessful) {
                    statusCounts = countsResponse.body()
                } else {
                    if (errorMessage == null) {
                        errorMessage = "Gagal memuat statistik artikel: ${countsResponse.code()}"
                    }
                }

                if (chartResponse.isSuccessful) {
                    keuanganChartData = chartResponse.body()
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
