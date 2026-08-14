package com.mharizkia.masjidmupro.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    @SerializedName("kata_sandi") val kataSandi: String,
    @SerializedName("masjid_id") val masjidId: Int? = null
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val nama: String,
    val email: String,
    val role: String? = null,
    @SerializedName("ranting_id") val rantingId: Int? = null,
    @SerializedName("preferred_masjid_id") val preferredMasjidId: Int?,
    @SerializedName("preferred_masjid") val preferredMasjid: Masjid?
)

data class UserRequest(
    val nama: String,
    val email: String,
    @SerializedName("kata_sandi") val kataSandi: String?,
    val role: String,
    @SerializedName("ranting_id") val rantingId: Int? = null
)

data class PaginatedUser(
    val data: List<User>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int
)

data class Masjid(
    val id: Int,
    @SerializedName("nama_masjid") val namaMasjid: String,
    @SerializedName("alamat_lengkap") val alamatLengkap: String?,
    @SerializedName("link_gmaps") val linkGmaps: String?,
    @SerializedName("link_instagram") val linkInstagram: String?,
    @SerializedName("link_youtube") val linkYoutube: String?,
    @SerializedName("link_facebook") val linkFacebook: String?,
    @SerializedName("link_tiktok") val linkTiktok: String?,
    @SerializedName("link_donasi") val linkDonasi: String?,
    @SerializedName("konten_visi_misi") val kontenVisiMisi: String?,
    @SerializedName("konten_sejarah") val kontenSejarah: String?
)

data class DashboardResponse(
    val masjid: Masjid,
    @SerializedName("upcoming_agenda") val upcomingAgenda: Int,
    @SerializedName("pending_berita") val pendingBerita: Int,
    @SerializedName("pending_artikel") val pendingArtikel: Int,
    @SerializedName("total_keuangan") val totalKeuangan: Double
)

data class ArtikelStatusCounts(
    val accepted: Int,
    val pending: Int,
    val rejected: Int,
    val total: Int
)

data class KeuanganChartDataset(
    val label: String,
    val data: List<Double>,
    val backgroundColor: String
)

data class KeuanganChartResponse(
    val labels: List<String>,
    val datasets: List<KeuanganChartDataset>
)

data class Keuangan(
    val id: Int,
    @SerializedName("profil_masjid_id") val profilMasjidId: Int,
    @SerializedName("judul_kegiatan") val judulKegiatan: String,
    @SerializedName("jumlah_biaya") val jumlahBiaya: Double,
    @SerializedName("jenis_biaya") val jenisBiaya: String?, // Masuk (Pemasukan) atau Keluar (Pengeluaran)
    @SerializedName("tanggal_kegiatan") val tanggalKegiatan: String
)

data class Berita(
    val id: Int,
    val judul: String,
    val slug: String? = null,
    val isi: String?,
    val deskripsi: String? = null,
    val status: String,
    @SerializedName("catatan_revisi") val catatanRevisi: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("gambar_flyer_url") val gambarFlyerUrl: String? = null
)

data class Artikel(
    val id: Int,
    val judul: String,
    val slug: String? = null,
    val konten: String?,
    val deskripsi: String? = null,
    val status: String,
    @SerializedName("catatan_revisi") val catatanRevisi: String?,
    @SerializedName("created_at") val createdAt: String,
    val pengguna: User? = null,
    @SerializedName("nama_penulis") val namaPenulis: String? = null,
    @SerializedName("gambar_flyer_url") val gambarFlyerUrl: String? = null
)

data class ReviewRequest(
    val action: String, // approve or reject
    @SerializedName("catatan_revisi") val catatanRevisi: String? = null
)

data class Agenda(
    val id: Int,
    val judul: String,
    val deskripsi: String?,
    val hari: String?,
    val tanggal: String,
    val waktu: String?,
    val status: String,
    @SerializedName("gambar_flyer_url") val gambarFlyerUrl: String?
)

data class PaginatedAgenda(
    val data: List<Agenda>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int
)

data class AgendaRequest(
    val judul: String,
    val deskripsi: String,
    val hari: String,
    val tanggal: String,
    val waktu: String,
    @SerializedName("tempat_link") val tempatLink: String,
    @SerializedName("tipe_acara") val tipeAcara: String,
    @SerializedName("penceramah_id") val penceramahId: Int? = null,
    val biaya: Int? = null,
    val cakupan: String? = null,
    @SerializedName("bidang_kajian") val bidangKajian: List<String>? = null,
    val kategori: List<String>? = null
)

data class Penceramah(
    val id: Int,
    @SerializedName("nama_penceramah") val namaPenceramah: String,
    @SerializedName("foto_profil_url") val fotoProfilUrl: String?
)

data class PaginatedPenceramah(
    val data: List<Penceramah>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int
)
