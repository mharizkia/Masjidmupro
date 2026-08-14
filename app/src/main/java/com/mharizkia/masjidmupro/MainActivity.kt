package com.mharizkia.masjidmupro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mharizkia.masjidmupro.data.AuthManager
import com.mharizkia.masjidmupro.ui.screens.*
import com.mharizkia.masjidmupro.ui.theme.MasjidmuPROTheme
import com.mharizkia.masjidmupro.ui.viewmodel.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MasjidmuPROTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val navController = rememberNavController()

    val startDestination = if (authManager.getToken() != null) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            val viewModel: LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return LoginViewModel(authManager) as T
                    }
                }
            )
            LoginScreen(viewModel = viewModel, onLoginSuccess = {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DashboardViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                DashboardScreen(
                    paddingValues = padding,
                    viewModel = viewModel,
                    onNavigateToKeuangan = { navController.navigate("keuangan") },
                    onNavigateToBerita = { navController.navigate("berita_pending") },
                    onNavigateToArtikel = { navController.navigate("artikel_pending") },
                    onNavigateToAgenda = { navController.navigate("agenda") }
                )
            }
        }
        composable("pengguna") {
            val viewModel: PenggunaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return PenggunaViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                PenggunaScreen(paddingValues = padding, viewModel = viewModel)
            }
        }
        composable("keuangan") {
            val viewModel: KeuanganViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return KeuanganViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                KeuanganScreen(paddingValues = padding, viewModel = viewModel)
            }
        }
        composable("berita_pending") {
            val viewModel: BeritaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return BeritaViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                PendingBeritaScreen(paddingValues = padding, viewModel = viewModel)
            }
        }
        composable("artikel_pending") {
            val viewModel: ArtikelViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ArtikelViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                PendingArtikelScreen(paddingValues = padding, viewModel = viewModel)
            }
        }
        composable("agenda") {
            val viewModel: AgendaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AgendaViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                AgendaScreen(paddingValues = padding, viewModel = viewModel)
            }
        }
        composable("profil") {
            val viewModel: ProfilViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ProfilViewModel(authManager) as T
                    }
                }
            )
            MainContainer(navController, onLogout = { logout(authManager, navController) }) { padding ->
                ProfilScreen(paddingValues = padding, viewModel = viewModel)
            }
        }
    }
}

fun logout(authManager: AuthManager, navController: androidx.navigation.NavController) {
    authManager.clearToken()
    navController.navigate("login") {
        popUpTo(0) { inclusive = true }
    }
}
