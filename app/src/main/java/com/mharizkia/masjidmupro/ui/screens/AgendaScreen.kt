package com.mharizkia.masjidmupro.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.FilterList
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
import com.mharizkia.masjidmupro.data.model.Agenda
import com.mharizkia.masjidmupro.data.model.AgendaRequest
import com.mharizkia.masjidmupro.ui.viewmodel.AgendaViewModel
import com.mharizkia.masjidmupro.utils.AppUtils
import java.util.*

@Composable
fun AgendaScreen(paddingValues: PaddingValues, viewModel: AgendaViewModel) {
    var selectedAgenda by remember { mutableStateOf<Agenda?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchAgendas()
    }

    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        if (selectedAgenda == null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ${if (viewModel.selectedStatus == "all") "Semua" else formatAgendaStatus(viewModel.selectedStatus)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    AgendaTableCell(text = "Judul", weight = 0.45f, isHeader = true)
                    AgendaTableCell(text = "Waktu", weight = 0.3f, isHeader = true)
                    AgendaTableCell(text = "Status", weight = 0.25f, isHeader = true)
                }

                if (viewModel.isLoading && viewModel.agendaList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(viewModel.agendaList) { agenda ->
                            AgendaRow(agenda, onClick = { selectedAgenda = agenda })
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Agenda")
            }
        } else {
            AgendaDetailContent(
                agenda = selectedAgenda!!,
                onBack = { selectedAgenda = null }
            )
        }
    }

    if (showAddDialog) {
        AddAgendaDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { request ->
                viewModel.storeAgenda(request) { success ->
                    if (success) showAddDialog = false
                }
            }
        )
    }

    if (showFilterSheet) {
        FilterAgendaDialog(
            viewModel = viewModel,
            onDismiss = { showFilterSheet = false },
            onApply = {
                viewModel.fetchAgendas()
                showFilterSheet = false
            }
        )
    }
}

@Composable
fun AgendaRow(agenda: Agenda, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgendaTableCell(text = agenda.judul, weight = 0.45f)
        AgendaTableCell(text = "${AppUtils.formatDateOnly(agenda.tanggal)}\n${agenda.waktu ?: ""}", weight = 0.3f)
        AgendaTableCell(
            text = formatAgendaStatus(agenda.status),
            weight = 0.25f,
            color = getAgendaStatusColor(agenda.status)
        )
    }
}

@Composable
fun RowScope.AgendaTableCell(
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
fun AgendaDetailContent(agenda: Agenda, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Detail Agenda", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = agenda.judul, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            
            agenda.gambarFlyerUrl?.let { url ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Flyer Agenda",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                    )
                }
            }

            Column {
                Text("Waktu & Tempat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${agenda.hari ?: ""}, ${AppUtils.formatDateOnly(agenda.tanggal)} pukul ${agenda.waktu ?: "-"}")
            }

            Column {
                Text("Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(AppUtils.stripHtml(agenda.deskripsi ?: "Tidak ada deskripsi"), style = MaterialTheme.typography.bodyLarge)
            }

            Surface(
                color = getAgendaStatusColor(agenda.status).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Status: ${formatAgendaStatus(agenda.status)}",
                    modifier = Modifier.padding(8.dp),
                    color = getAgendaStatusColor(agenda.status),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgendaDialog(viewModel: AgendaViewModel, onDismiss: () -> Unit, onSave: (AgendaRequest) -> Unit) {
    val context = LocalContext.current
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var hari by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }
    var tempatLink by remember { mutableStateOf("") }
    var tipeAcara by remember { mutableStateOf("offline") }
    var selectedPenceramahId by remember { mutableStateOf<Int?>(null) }
    var penceramahExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchPenceramah()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Agenda") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = judul, onValueChange = { judul = it }, label = { Text("Judul Agenda") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = deskripsi, onValueChange = { deskripsi = it }, label = { Text("Deskripsi") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                
                // Penceramah Dropdown
                Column {
                    Text("Pilih Penceramah (Opsional)", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { penceramahExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = viewModel.penceramahList.find { it.id == selectedPenceramahId }?.namaPenceramah ?: "-- Tanpa Penceramah --",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = penceramahExpanded,
                            onDismissRequest = { penceramahExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("-- Tanpa Penceramah --") },
                                onClick = {
                                    selectedPenceramahId = null
                                    penceramahExpanded = false
                                }
                            )
                            viewModel.penceramahList.forEach { penceramah ->
                                DropdownMenuItem(
                                    text = { Text(penceramah.namaPenceramah) },
                                    onClick = {
                                        selectedPenceramahId = penceramah.id
                                        penceramahExpanded = false
                                    }
                                )
                            }
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
                            val mm = String.format(Locale.US, "%02d", m + 1)
                            val dd = String.format(Locale.US, "%02d", d)
                            tanggal = "$y-$mm-$dd"
                            
                            val selectedCal = Calendar.getInstance()
                            selectedCal.set(y, m, d)
                            hari = when (selectedCal.get(Calendar.DAY_OF_WEEK)) {
                                Calendar.MONDAY -> "Senin"
                                Calendar.TUESDAY -> "Selasa"
                                Calendar.WEDNESDAY -> "Rabu"
                                Calendar.THURSDAY -> "Kamis"
                                Calendar.FRIDAY -> "Jumat"
                                Calendar.SATURDAY -> "Sabtu"
                                Calendar.SUNDAY -> "Ahad"
                                else -> ""
                            }
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

                OutlinedTextField(value = hari, onValueChange = { hari = it }, label = { Text("Hari") })
                OutlinedTextField(value = waktu, onValueChange = { waktu = it }, label = { Text("Waktu (contoh: 19:30)") })
                OutlinedTextField(value = tempatLink, onValueChange = { tempatLink = it }, label = { Text("Tempat / Link") })
                
                Text("Tipe Acara", style = MaterialTheme.typography.labelLarge)
                Row {
                    listOf("offline", "online", "hybrid").forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tipeAcara = type }) {
                            RadioButton(selected = tipeAcara == type, onClick = { tipeAcara = type })
                            Text(type.replaceFirstChar { it.uppercase(Locale.getDefault()) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (judul.isNotBlank() && tanggal.isNotBlank()) {
                    onSave(
                        AgendaRequest(
                            judul = judul,
                            deskripsi = deskripsi,
                            hari = hari,
                            tanggal = tanggal,
                            waktu = waktu,
                            tempatLink = tempatLink,
                            tipeAcara = tipeAcara,
                            penceramahId = selectedPenceramahId
                        )
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun FilterAgendaDialog(viewModel: AgendaViewModel, onDismiss: () -> Unit, onApply: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Status") },
        text = {
            Column {
                listOf("all" to "Semua", "published_masjid" to "Terbit", "pending_penceramah" to "Menunggu Penceramah").forEach { (valKey, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.selectedStatus = valKey }) {
                        RadioButton(selected = viewModel.selectedStatus == valKey, onClick = { viewModel.selectedStatus = valKey })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onApply) { Text("Terapkan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

fun formatAgendaStatus(status: String): String {
    return when (status) {
        "published_masjid" -> "Terbit"
        "pending_penceramah" -> "Menunggu Penceramah"
        else -> status
    }
}

@Composable
fun getAgendaStatusColor(status: String): Color {
    return when (status) {
        "published_masjid" -> Color(0xFF4CAF50)
        "pending_penceramah" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
}
