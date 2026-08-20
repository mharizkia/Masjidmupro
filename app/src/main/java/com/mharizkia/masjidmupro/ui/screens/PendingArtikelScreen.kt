package com.mharizkia.masjidmupro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mharizkia.masjidmupro.data.model.Artikel
import com.mharizkia.masjidmupro.ui.viewmodel.ArtikelViewModel
import com.mharizkia.masjidmupro.utils.AppUtils

@Composable
fun PendingArtikelScreen(paddingValues: PaddingValues, viewModel: ArtikelViewModel) {
    var selectedArtikel by remember { mutableStateOf<Artikel?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchPendingArtikel()
    }

    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        if (selectedArtikel == null) {
            ArtikelListContent(
                viewModel = viewModel,
                onFilterClick = { showFilterDialog = true },
                onArtikelClick = { selectedArtikel = it }
            )
        } else {
            ArtikelDetailContent(
                artikel = selectedArtikel!!,
                onBack = { selectedArtikel = null },
                onReview = { action, catatan ->
                    viewModel.reviewArtikel(selectedArtikel!!, action, catatan) { success ->
                        if (success) selectedArtikel = null
                    }
                }
            )
        }
    }

    if (showFilterDialog) {
        FilterArtikelDialog(
            viewModel = viewModel,
            onDismiss = { showFilterDialog = false },
            onApply = {
                viewModel.fetchPendingArtikel()
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun ArtikelListContent(
    viewModel: ArtikelViewModel,
    onFilterClick: () -> Unit,
    onArtikelClick: (Artikel) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Cari judul atau penulis...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }

        // Selected filter summary
        if (viewModel.selectedStatus != "all") {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ${formatStatus(viewModel.selectedStatus)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .size(14.dp)
                            .clickable {
                                viewModel.selectedStatus = "all"
                                viewModel.fetchPendingArtikel()
                            }
                    )
                }
            }
        }

        if (viewModel.isLoading && viewModel.pendingArtikel.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header - Using weighted columns (no fixed width)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    TableCell(text = "Penulis", weight = 0.35f, isHeader = true)
                    TableCell(text = "Judul", weight = 0.40f, isHeader = true)
                    TableCell(text = "Status", weight = 0.25f, isHeader = true)
                }

                // Table Body
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.filteredArtikel) { artikel ->
                        ArtikelRow(artikel, onClick = { onArtikelClick(artikel) })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ArtikelRow(artikel: Artikel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = artikel.namaPenulis ?: artikel.pengguna?.nama ?: "-", weight = 0.35f)
        TableCell(text = artikel.judul, weight = 0.40f)
        TableCell(
            text = formatStatus(artikel.status),
            weight = 0.25f,
            color = getStatusColor(artikel.status)
        )
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtikelDetailContent(
    artikel: Artikel,
    onBack: () -> Unit,
    onReview: (String, String?) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var catatanRevisi by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Review Artikel", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = artikel.judul, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Oleh: ${artikel.namaPenulis ?: artikel.pengguna?.nama ?: "-"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Tanggal: ${AppUtils.formatDateOnly(artikel.createdAt)}", style = MaterialTheme.typography.labelSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image Flyer
            artikel.gambarFlyerUrl?.let { url ->
                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Flyer Artikel",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            val rawContent = artikel.deskripsi ?: artikel.konten ?: "Tidak ada konten"
            Text(text = AppUtils.stripHtml(rawContent), style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showRejectDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tolak")
            }
            Button(
                onClick = { onReview("approve", null) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Terima")
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Tolak Artikel") },
            text = {
                Column {
                    Text("Berikan catatan revisi untuk penulis:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catatanRevisi,
                        onValueChange = { catatanRevisi = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Catatan Revisi") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReview("reject", catatanRevisi)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Kirim Penolakan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun FilterArtikelDialog(
    viewModel: ArtikelViewModel,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Status Artikel") },
        text = {
            Column {
                val options = listOf(
                    "all" to "Semua Pending",
                    "pending_masjid" to "Menunggu Review",
                    "published_masjid" to "Terbit",
                    "rejected_masjid" to "Ditolak"
                )
                
                options.forEach { (valKey, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedStatus = valKey }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = viewModel.selectedStatus == valKey,
                            onClick = { viewModel.selectedStatus = valKey }
                        )
                        Text(text = label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onApply) { Text("Terapkan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

fun formatStatus(status: String): String {
    return when (status) {
        "pending_masjid" -> "Menunggu"
        "rejected_masjid" -> "Ditolak"
        "published_masjid" -> "Terbit"
        else -> status
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "pending_masjid" -> MaterialTheme.colorScheme.primary
        "rejected_masjid" -> MaterialTheme.colorScheme.error
        "published_masjid" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurface
    }
}
