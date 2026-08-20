package com.mharizkia.masjidmupro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mharizkia.masjidmupro.data.model.User
import com.mharizkia.masjidmupro.data.model.UserRequest
import com.mharizkia.masjidmupro.ui.viewmodel.PenggunaViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PenggunaScreen(paddingValues: PaddingValues, viewModel: PenggunaViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var searchInput by remember { mutableStateOf(viewModel.searchQuery) }

    LaunchedEffect(Unit) {
        if (viewModel.users.isEmpty()) {
            viewModel.fetchUsers()
        }
    }

    LaunchedEffect(searchInput) {
        if (searchInput != viewModel.searchQuery) {
            delay(500.milliseconds) // Debounce
            viewModel.searchQuery = searchInput
            viewModel.fetchUsers()
        }
    }

    Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Cari nama atau email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter Role",
                    tint = if (viewModel.selectedRoles.isNotEmpty()) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        // Active filters display using LazyRow for horizontal scrolling
        if (viewModel.selectedRoles.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(viewModel.selectedRoles.toList()) { role ->
                    val label = when(role) {
                        "pengurus_masjid" -> "Pengurus"
                        "ustadz" -> "Ustadz"
                        "jamaah" -> "Jamaah"
                        else -> role
                    }
                    FilterChip(
                        selected = true,
                        onClick = {
                            viewModel.selectedRoles = viewModel.selectedRoles - role
                            viewModel.fetchUsers()
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                    )
                }
                
                item {
                    TextButton(onClick = { 
                        viewModel.selectedRoles = emptySet()
                        viewModel.fetchUsers()
                    }) {
                        Text("Hapus Semua", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (viewModel.isLoading && viewModel.users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (viewModel.users.isEmpty() && !viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada pengguna ditemukan", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(viewModel.users) { user ->
                            UserItem(
                                user = user,
                                onEdit = {
                                    selectedUser = user
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteUser(user.id) }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    selectedUser = null
                    showDialog = true
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengguna")
            }

            if (showDialog) {
                UserFormDialog(
                    user = selectedUser,
                    onDismiss = { showDialog = false },
                    onSave = { request ->
                        viewModel.saveUser(selectedUser?.id, request) { success ->
                            if (success) showDialog = false
                        }
                    }
                )
            }
        }
    }

    if (showFilterDialog) {
        RoleFilterDialog(
            selectedRoles = viewModel.selectedRoles,
            onDismiss = { showFilterDialog = false },
            onApply = { roles ->
                viewModel.selectedRoles = roles
                viewModel.fetchUsers()
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun RoleFilterDialog(
    selectedRoles: Set<String>,
    onDismiss: () -> Unit,
    onApply: (Set<String>) -> Unit
) {
    var tempRoles by remember { mutableStateOf(selectedRoles) }
    val roleOptions = listOf(
        "pengurus_masjid" to "Pengurus Masjid",
        "ustadz" to "Ustadz",
        "jamaah" to "Jamaah"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Berdasarkan Role") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                roleOptions.forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable {
                            tempRoles = if (tempRoles.contains(value)) tempRoles - value else tempRoles + value
                        }
                    ) {
                        Checkbox(
                            checked = tempRoles.contains(value),
                            onCheckedChange = { checked ->
                                tempRoles = if (checked) tempRoles + value else tempRoles - value
                            }
                        )
                        Text(text = label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(tempRoles) }) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                tempRoles = emptySet()
                onApply(emptySet())
            }) {
                Text("Reset")
            }
        }
    )
}

@Composable
fun UserItem(user: User, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.nama, style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Role: ${user.role ?: "-"}", style = MaterialTheme.typography.labelSmall)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormDialog(user: User?, onDismiss: () -> Unit, onSave: (UserRequest) -> Unit) {
    var nama by remember { mutableStateOf(user?.nama ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var kataSandi by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "pengurus_masjid") }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf(
        "pengurus_masjid" to "Pengurus Masjid",
        "ustadz" to "Ustadz",
        "jamaah" to "Jamaah"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Tambah Pengguna" else "Edit Pengguna") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = kataSandi,
                    onValueChange = { kataSandi = it },
                    label = { Text("Kata Sandi (Kosongkan jika tidak diubah)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = roles.find { it.first == role }?.second ?: role,
                        onValueChange = { },
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        roles.forEach { (roleValue, roleLabel) ->
                            DropdownMenuItem(
                                text = { Text(roleLabel) },
                                onClick = {
                                    role = roleValue
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(UserRequest(nama, email, if (kataSandi.isEmpty()) null else kataSandi, role))
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
