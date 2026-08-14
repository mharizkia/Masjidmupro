package com.mharizkia.masjidmupro.data.remote

import com.mharizkia.masjidmupro.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Headers("Accept: application/json")
    @POST("pengurus/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Headers("Accept: application/json")
    @GET("pengurus/dashboard")
    suspend fun getDashboard(@Header("Authorization") token: String): Response<DashboardResponse>

    @Headers("Accept: application/json")
    @GET("pengurus/keuangan")
    suspend fun getKeuangan(
        @Header("Authorization") token: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("jenis_biaya") jenisBiaya: String? = null
    ): Response<List<Keuangan>>

    @Headers("Accept: application/json")
    @GET("pengurus/keuangan/chart")
    suspend fun getKeuanganChart(
        @Header("Authorization") token: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<KeuanganChartResponse>

    @Headers("Accept: application/json")
    @GET("pengurus/keuangan/parents")
    suspend fun getParentKeuangan(@Header("Authorization") token: String): Response<List<Keuangan>>

    @Headers("Accept: application/json")
    @POST("pengurus/keuangan")
    suspend fun storeKeuangan(
        @Header("Authorization") token: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Keuangan>

    @Headers("Accept: application/json")
    @GET("pengurus/berita/pending")
    suspend fun getPendingBerita(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<List<Berita>>

    @Headers("Accept: application/json")
    @POST("pengurus/berita")
    suspend fun storeBerita(
        @Header("Authorization") token: String,
        @Body request: @JvmSuppressWildcards Map<String, Any?>
    ): Response<Berita>

    @Headers("Accept: application/json")
    @POST("pengurus/berita/{identifier}/review")
    suspend fun reviewBerita(
        @Header("Authorization") token: String,
        @Path("identifier") identifier: String,
        @Body request: ReviewRequest
    ): Response<Berita>

    @Headers("Accept: application/json")
    @GET("pengurus/artikel/pending")
    suspend fun getPendingArtikel(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<List<Artikel>>

    @Headers("Accept: application/json")
    @GET("pengurus/artikel/status-counts")
    suspend fun getArtikelStatusCounts(@Header("Authorization") token: String): Response<ArtikelStatusCounts>

    @Headers("Accept: application/json")
    @POST("pengurus/artikel/{identifier}/review")
    suspend fun reviewArtikel(
        @Header("Authorization") token: String,
        @Path("identifier") identifier: String,
        @Body request: ReviewRequest
    ): Response<Artikel>

    @Headers("Accept: application/json")
    @GET("pengurus/profil")
    suspend fun getProfil(@Header("Authorization") token: String): Response<Masjid>

    @Headers("Accept: application/json")
    @PUT("pengurus/profil")
    suspend fun updateProfil(
        @Header("Authorization") token: String,
        @Body masjid: @JvmSuppressWildcards Map<String, Any?>
    ): Response<Masjid>

    @Headers("Accept: application/json")
    @GET("pengurus/agenda")
    suspend fun listAgenda(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<PaginatedAgenda>

    @Headers("Accept: application/json")
    @POST("pengurus/agenda")
    suspend fun storeAgenda(
        @Header("Authorization") token: String,
        @Body request: AgendaRequest
    ): Response<Agenda>

    @Headers("Accept: application/json")
    @GET("pengurus/penceramah")
    suspend fun listPenceramah(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1
    ): Response<PaginatedPenceramah>

    // Manajemen Pengguna
    @Headers("Accept: application/json")
    @GET("pengurus/pengguna")
    suspend fun listPengguna(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null
    ): Response<PaginatedUser>

    @Headers("Accept: application/json")
    @GET("pengurus/pengguna/{id}")
    suspend fun showPengguna(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<User>

    @Headers("Accept: application/json")
    @POST("pengurus/pengguna")
    suspend fun storePengguna(
        @Header("Authorization") token: String,
        @Body request: UserRequest
    ): Response<User>

    @Headers("Accept: application/json")
    @PUT("pengurus/pengguna/{id}")
    suspend fun updatePengguna(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UserRequest
    ): Response<User>

    @Headers("Accept: application/json")
    @DELETE("pengurus/pengguna/{id}")
    suspend fun destroyPengguna(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, String>>
}
