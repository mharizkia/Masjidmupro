package com.mharizkia.masjidmupro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
            viewModel.onSearchQueryChanged(searchInput)
        }
    }

    Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
        OutlinedTextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Cari nama atau email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (viewModel.isLoading && viewModel.users.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                
                // Role Dropdown
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
