package com.mharizkia.masjidmupro.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mharizkia.masjidmupro.data.model.Berita
import com.mharizkia.masjidmupro.ui.viewmodel.BeritaViewModel
import java.util.*

@Composable
fun PendingBeritaScreen(paddingValues: PaddingValues, viewModel: BeritaViewModel) {
    var selectedBerita by remember { mutableStateOf<Berita?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchPendingBerita()
    }

    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        if (selectedBerita == null) {
            BeritaListContent(
                viewModel = viewModel,
                onFilterClick = { showFilterDialog = true },
                onBeritaClick = { selectedBerita = it }
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Berita")
            }
        } else {
            BeritaDetailContent(
                berita = selectedBerita!!,
                onBack = { selectedBerita = null },
                onReview = { action, catatan ->
                    viewModel.reviewBerita(selectedBerita!!, action, catatan) { success ->
                        if (success) selectedBerita = null
                    }
                }
            )
        }
    }

    if (showFilterDialog) {
        FilterBeritaDialog(
            viewModel = viewModel,
            onDismiss = { showFilterDialog = false },
            onApply = {
                viewModel.fetchPendingBerita()
                showFilterDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddBeritaDialog(
            onDismiss = { showAddDialog = false },
            onSave = { judul, deskripsi, hari, tanggal, jam, penulis ->
                viewModel.storeBerita(judul, deskripsi, hari, tanggal, jam, penulis) { success ->
                    if (success) showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun BeritaListContent(
    viewModel: BeritaViewModel,
    onFilterClick: () -> Unit,
    onBeritaClick: (Berita) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                placeholder = { Text("Cari judul berita...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }

        if (viewModel.selectedStatus != "all") {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ${formatBeritaStatus(viewModel.selectedStatus)}",
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
                                viewModel.fetchPendingBerita()
                            }
                    )
                }
            }
        }

        if (viewModel.isLoading && viewModel.pendingBerita.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    BeritaTableCell(text = "Judul Berita", weight = 0.7f, isHeader = true)
                    BeritaTableCell(text = "Status", weight = 0.3f, isHeader = true)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.filteredBerita) { berita ->
                        BeritaRow(berita, onClick = { onBeritaClick(berita) })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun BeritaRow(berita: Berita, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BeritaTableCell(text = berita.judul, weight = 0.7f)
        BeritaTableCell(
            text = formatBeritaStatus(berita.status),
            weight = 0.3f,
            color = getBeritaStatusColor(berita.status)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeritaDetailContent(
    berita: Berita,
    onBack: () -> Unit,
    onReview: (String, String?) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var catatanRevisi by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Review Berita", style = MaterialTheme.typography.titleMedium) },
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
            Text(text = berita.judul, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "Tanggal: ${berita.createdAt}", style = MaterialTheme.typography.labelSmall)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Image Flyer
            berita.gambarFlyerUrl?.let { url ->
                Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Flyer Berita",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            val displayContent = berita.deskripsi ?: berita.isi ?: "Tidak ada isi berita"
            Text(text = displayContent, style = MaterialTheme.typography.bodyLarge)
            
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
            title = { Text("Tolak Berita") },
            text = {
                Column {
                    Text("Berikan catatan revisi untuk penulis berita:")
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
fun FilterBeritaDialog(
    viewModel: BeritaViewModel,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Status Berita") },
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

@Composable
fun AddBeritaDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String, String?) -> Unit) {
    val context = LocalContext.current
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var hari by remember { mutableStateOf("Senin") }
    var tanggal by remember { mutableStateOf("") }
    var jam by remember { mutableStateOf("") }
    var penulis by remember { mutableStateOf("") }
    
    var expandedHari by remember { mutableStateOf(false) }
    val daftarHari = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Input Berita Baru") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = judul, 
                    onValueChange = { judul = it }, 
                    label = { Text("Judul Berita") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = deskripsi, 
                    onValueChange = { deskripsi = it }, 
                    label = { Text("Deskripsi / Isi Berita") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Box {
                    OutlinedTextField(
                        value = hari,
                        onValueChange = { },
                        label = { Text("Hari") },
                        modifier = Modifier.fillMaxWidth().clickable { expandedHari = true },
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = expandedHari,
                        onDismissRequest = { expandedHari = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        daftarHari.forEach { h ->
                            DropdownMenuItem(
                                text = { Text(h) },
                                onClick = {
                                    hari = h
                                    expandedHari = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { },
                    label = { Text("Tanggal") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            val selectedCal = Calendar.getInstance()
                            selectedCal.set(y, m, d)
                            
                            val dayOfWeek = selectedCal.get(Calendar.DAY_OF_WEEK)
                            hari = when (dayOfWeek) {
                                Calendar.MONDAY -> "Senin"
                                Calendar.TUESDAY -> "Selasa"
                                Calendar.WEDNESDAY -> "Rabu"
                                Calendar.THURSDAY -> "Kamis"
                                Calendar.FRIDAY -> "Jumat"
                                Calendar.SATURDAY -> "Sabtu"
                                Calendar.SUNDAY -> "Minggu"
                                else -> hari
                            }

                            tanggal = "$y-${String.format(Locale.US, "%02d", m + 1)}-${String.format(Locale.US, "%02d", d)}"
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = jam,
                    onValueChange = { },
                    label = { Text("Jam") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(context, { _, h, m ->
                            jam = "${String.format(Locale.US, "%02d", h)}:${String.format(Locale.US, "%02d", m)}"
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = penulis, 
                    onValueChange = { penulis = it }, 
                    label = { Text("Nama Penulis (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (judul.isNotBlank() && deskripsi.isNotBlank() && tanggal.isNotBlank() && jam.isNotBlank()) {
                        // Map "Minggu" to "Ahad" for backend compatibility
                        val hariUntukBackend = if (hari == "Minggu") "Ahad" else hari
                        onSave(judul, deskripsi, hariUntukBackend, tanggal, jam, penulis.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Red)
            }
        }
    )
}

@Composable
fun RowScope.BeritaTableCell(
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

fun formatBeritaStatus(status: String): String {
    return when (status) {
        "pending_masjid" -> "Menunggu"
        "rejected_masjid" -> "Ditolak"
        "published_masjid" -> "Terbit"
        else -> status
    }
}

@Composable
fun getBeritaStatusColor(status: String): Color {
    return when (status) {
        "pending_masjid" -> MaterialTheme.colorScheme.primary
        "rejected_masjid" -> MaterialTheme.colorScheme.error
        "published_masjid" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurface
    }
}
