package com.mharizkia.masjidmupro.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mharizkia.masjidmupro.data.model.Keuangan
import com.mharizkia.masjidmupro.ui.viewmodel.KeuanganViewModel
import java.text.NumberFormat
import java.util.*

// Constants for JenisBiaya Enum values from Backend
const val JENIS_MASUK = "Masuk (Pemasukan)"
const val JENIS_KELUAR = "Keluar (Pengeluaran)"

@Composable
fun KeuanganScreen(paddingValues: PaddingValues, viewModel: KeuanganViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchKeuangan()
    }

    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with filters summary
            FilterSummaryRow(
                onFilterClick = { showFilterSheet = true },
                viewModel = viewModel
            )

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                KeuanganTableCell(text = "Tanggal", weight = 0.3f, isHeader = true)
                KeuanganTableCell(text = "Masuk (Rp)", weight = 0.35f, isHeader = true, textAlign = TextAlign.End)
                KeuanganTableCell(text = "Keluar (Rp)", weight = 0.35f, isHeader = true, textAlign = TextAlign.End)
            }

            if (viewModel.isLoading && viewModel.keuanganList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(viewModel.keuanganList) { item ->
                        KeuanganRow(item)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Keuangan")
        }
    }

    if (showAddDialog) {
        AddKeuanganDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { judul, tanggal, jumlah, jenis, parentId ->
                viewModel.storeKeuangan(judul, tanggal, jumlah, jenis, parentId) { success ->
                    if (success) showAddDialog = false
                }
            }
        )
    }

    if (showFilterSheet) {
        FilterKeuanganDialog(
            viewModel = viewModel,
            onDismiss = { showFilterSheet = false },
            onApply = {
                viewModel.fetchKeuangan()
                showFilterSheet = false
            }
        )
    }
}

@Composable
fun FilterSummaryRow(onFilterClick: () -> Unit, viewModel: KeuanganViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (viewModel.startDate.isEmpty() && viewModel.endDate.isEmpty()) "Semua Waktu"
                else "${viewModel.startDate} s/d ${viewModel.endDate}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Jenis: ${if (viewModel.selectedJenis == "all") "Semua" else viewModel.selectedJenis}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter")
        }
    }
}

@Composable
fun KeuanganRow(item: Keuangan) {
    val amount = formatCurrency(item.jumlahBiaya)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeuanganTableCell(text = item.tanggalKegiatan, weight = 0.3f)
        
        // Income column
        KeuanganTableCell(
            text = if (item.jenisBiaya == JENIS_MASUK) amount else "-",
            weight = 0.35f,
            textAlign = TextAlign.End,
            color = if (item.jenisBiaya == JENIS_MASUK) Color(0xFF4CAF50) else Color.Unspecified
        )
        
        // Expense column
        KeuanganTableCell(
            text = if (item.jenisBiaya == JENIS_KELUAR) amount else "-",
            weight = 0.35f,
            textAlign = TextAlign.End,
            color = if (item.jenisBiaya == JENIS_KELUAR) MaterialTheme.colorScheme.error else Color.Unspecified
        )
    }
}

@Composable
fun RowScope.KeuanganTableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        textAlign = textAlign,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKeuanganDialog(
    viewModel: KeuanganViewModel,
    onDismiss: () -> Unit, 
    onSave: (String, String, Double?, String, Int?) -> Unit
) {
    val context = LocalContext.current
    var judul by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf(JENIS_MASUK) }
    var selectedParentId by remember { mutableStateOf<Int?>(null) }
    var selectedParentLabel by remember { mutableStateOf("-- Tidak Ada (Buat Kegiatan Utama Baru) --") }
    var expanded by remember { mutableStateOf(false) }

    // Use current items as parent options
    val parentOptions = viewModel.keuanganList

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Input Keuangan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = judul, 
                    onValueChange = { judul = it }, 
                    label = { Text("JUDUL KEGIATAN") },
                    placeholder = { Text("Contoh: Infaq Jumat, Pembelian Karpet...") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = tanggal, 
                    onValueChange = { }, 
                    label = { Text("TANGGAL") },
                    placeholder = { Text("Pilih Tanggal") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                tanggal = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Column {
                    Text("PARENT KEGIATAN (OPSIONAL)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedParentLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("-- Tidak Ada (Utama) --") },
                                onClick = {
                                    selectedParentId = null
                                    selectedParentLabel = "-- Tidak Ada (Buat Kegiatan Utama Baru) --"
                                    expanded = false
                                }
                            )
                            
                            parentOptions.forEach { parent ->
                                DropdownMenuItem(
                                    text = { Text(parent.judulKegiatan) },
                                    onClick = {
                                        selectedParentId = parent.id
                                        selectedParentLabel = parent.judulKegiatan
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text("Pilih jika ini adalah sub-transaksi dari kegiatan yang sudah ada.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                OutlinedTextField(
                    value = jumlah, 
                    onValueChange = { jumlah = it }, 
                    label = { Text("JUMLAH BIAYA (RP) (OPSIONAL, DEFAULT 0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text("JENIS TRANSAKSI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { jenis = JENIS_MASUK },
                            modifier = Modifier.weight(1f),
                            colors = if (jenis == JENIS_MASUK) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Masuk (Pemasukan)")
                        }
                        OutlinedButton(
                            onClick = { jenis = JENIS_KELUAR },
                            modifier = Modifier.weight(1f),
                            colors = if (jenis == JENIS_KELUAR) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Keluar (Pengeluaran)")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = jumlah.toDoubleOrNull()
                    onSave(judul, tanggal, amount, jenis, selectedParentId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun FilterKeuanganDialog(
    viewModel: KeuanganViewModel,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Keuangan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Start Date Picker
                OutlinedTextField(
                    value = viewModel.startDate,
                    onValueChange = { },
                    label = { Text("Dari Tanggal") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.startDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // End Date Picker
                OutlinedTextField(
                    value = viewModel.endDate,
                    onValueChange = { },
                    label = { Text("Sampai Tanggal") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.endDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text("Jenis Biaya", style = MaterialTheme.typography.labelLarge)
                Column {
                    listOf("all" to "Semua", JENIS_MASUK to "Masuk", JENIS_KELUAR to "Keluar").forEach { (valKey, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.selectedJenis = valKey }) {
                            RadioButton(selected = viewModel.selectedJenis == valKey, onClick = { viewModel.selectedJenis = valKey })
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onApply) { Text("Terapkan") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.startDate = ""
                viewModel.endDate = ""
                viewModel.selectedJenis = "all"
                onApply()
            }) { Text("Reset") }
        }
    )
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "").trim()
}
