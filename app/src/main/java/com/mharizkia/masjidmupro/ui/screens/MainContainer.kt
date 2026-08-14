package com.mharizkia.masjidmupro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Profil : Screen("profil", "Profil Masjid", Icons.Default.AccountBalance)
    object Pengguna : Screen("pengguna", "Pengguna", Icons.Default.People)
    object Agenda : Screen("agenda", "Agenda", Icons.Default.Event)
    object Artikel : Screen("artikel_pending", "Artikel", Icons.Default.Description)
    object Berita : Screen("berita_pending", "Berita", Icons.Default.Newspaper)
    object Keuangan : Screen("keuangan", "Keuangan", Icons.Default.AccountBalanceWallet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    navController: NavController,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Masjidmu PRO",
                    modifier = Modifier.padding(horizontal = 28.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                NavigationItem(
                    screen = Screen.Dashboard,
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )

                NavigationItem(
                    screen = Screen.Profil,
                    selected = currentRoute == Screen.Profil.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Profil.route)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Manajemen",
                    modifier = Modifier.padding(start = 28.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )

                listOf(Screen.Pengguna, Screen.Agenda, Screen.Artikel, Screen.Berita, Screen.Keuangan).forEach { screen ->
                    NavigationItem(
                        screen = screen,
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp), color = MaterialTheme.colorScheme.outlineVariant)
                NavigationDrawerItem(
                    label = { Text("Keluar", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        val title = when (currentRoute) {
                            Screen.Dashboard.route -> "Dashboard"
                            Screen.Profil.route -> "Profil Masjid"
                            Screen.Pengguna.route -> "Pengguna"
                            Screen.Agenda.route -> "Agenda"
                            Screen.Artikel.route -> "Review Artikel"
                            Screen.Berita.route -> "Review Berita"
                            Screen.Keuangan.route -> "Keuangan"
                            else -> "Masjidmu PRO"
                        }
                        Text(title, fontWeight = FontWeight.SemiBold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                content(padding)
            }
        }
    }
}

@Composable
fun NavigationItem(screen: Screen, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(screen.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(screen.icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedContainerColor = Color.Transparent
        )
    )
}
