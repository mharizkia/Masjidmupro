package com.mharizkia.masjidmupro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mharizkia.masjidmupro.ui.viewmodel.ProfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(paddingValues: PaddingValues, viewModel: ProfilViewModel) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.fetchProfil()
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            snackbarHostState.showSnackbar("Profil berhasil diperbarui")
        }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.masjidData?.let { data ->
                    viewModel.updateProfil(
                        namaMasjid = data.namaMasjid,
                        alamat = data.alamatLengkap ?: "",
                        gmaps = data.linkGmaps,
                        instagram = data.linkInstagram,
                        youtube = data.linkYoutube,
                        facebook = data.linkFacebook,
                        tiktok = data.linkTiktok,
                        donasi = data.linkDonasi,
                        visiMisi = data.kontenVisiMisi,
                        sejarah = data.kontenSejarah
                    )
                }
            }) {
                Icon(Icons.Default.Save, contentDescription = "Simpan")
            }
        }
    ) { innerPadding ->
        if (viewModel.isLoading && viewModel.masjidData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            viewModel.masjidData?.let { data ->
                var namaMasjid by remember(data) { mutableStateOf(data.namaMasjid) }
                var alamat by remember(data) { mutableStateOf(data.alamatLengkap ?: "") }
                var gmaps by remember(data) { mutableStateOf(data.linkGmaps ?: "") }
                var instagram by remember(data) { mutableStateOf(data.linkInstagram ?: "") }
                var youtube by remember(data) { mutableStateOf(data.linkYoutube ?: "") }
                var facebook by remember(data) { mutableStateOf(data.linkFacebook ?: "") }
                var tiktok by remember(data) { mutableStateOf(data.linkTiktok ?: "") }
                var donasi by remember(data) { mutableStateOf(data.linkDonasi ?: "") }
                var visiMisi by remember(data) { mutableStateOf(data.kontenVisiMisi ?: "") }
                var sejarah by remember(data) { mutableStateOf(data.kontenSejarah ?: "") }

                // Local update to keep state in sync for the FAB action
                // Note: Better to handle form state in ViewModel, but for now this works with the FAB above
                data.apply {
                    // This is a bit hacky but works for a single-file edit
                    // Ideally, the FAB should be inside the Column to access these states directly
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Informasi Dasar", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = namaMasjid,
                        onValueChange = { namaMasjid = it; viewModel.masjidData = data.copy(namaMasjid = it) },
                        label = { Text("Nama Masjid") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alamat,
                        onValueChange = { alamat = it; viewModel.masjidData = data.copy(alamatLengkap = it) },
                        label = { Text("Alamat Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Text("Media Sosial & Donasi", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = gmaps,
                        onValueChange = { gmaps = it; viewModel.masjidData = data.copy(linkGmaps = it) },
                        label = { Text("Link Google Maps") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instagram,
                        onValueChange = { instagram = it; viewModel.masjidData = data.copy(linkInstagram = it) },
                        label = { Text("Link Instagram") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = youtube,
                        onValueChange = { youtube = it; viewModel.masjidData = data.copy(linkYoutube = it) },
                        label = { Text("Link YouTube") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = facebook,
                        onValueChange = { facebook = it; viewModel.masjidData = data.copy(linkFacebook = it) },
                        label = { Text("Link Facebook") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tiktok,
                        onValueChange = { tiktok = it; viewModel.masjidData = data.copy(linkTiktok = it) },
                        label = { Text("Link TikTok") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = donasi,
                        onValueChange = { donasi = it; viewModel.masjidData = data.copy(linkDonasi = it) },
                        label = { Text("Link Donasi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Konten Tambahan", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = visiMisi,
                        onValueChange = { visiMisi = it; viewModel.masjidData = data.copy(kontenVisiMisi = it) },
                        label = { Text("Visi & Misi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = sejarah,
                        onValueChange = { sejarah = it; viewModel.masjidData = data.copy(kontenSejarah = it) },
                        label = { Text("Sejarah Masjid") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    if (viewModel.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    viewModel.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
        }
    }
}
