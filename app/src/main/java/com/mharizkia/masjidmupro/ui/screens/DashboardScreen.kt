package com.mharizkia.masjidmupro.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mharizkia.masjidmupro.data.model.ArtikelStatusCounts
import com.mharizkia.masjidmupro.data.model.KeuanganChartResponse
import com.mharizkia.masjidmupro.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    viewModel: DashboardViewModel,
    onNavigateToKeuangan: () -> Unit,
    onNavigateToBerita: () -> Unit,
    onNavigateToArtikel: () -> Unit,
    onNavigateToAgenda: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchDashboard()
    }

    if (viewModel.isLoading && viewModel.dashboardData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        viewModel.dashboardData?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = data.masjid.namaMasjid, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(text = data.masjid.alamatLengkap ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Pie Chart Section
                item {
                    if (viewModel.statusCounts == null && viewModel.isLoading) {
                        Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    } else {
                        ArtikelStatusPieChart(viewModel.statusCounts ?: ArtikelStatusCounts(0, 0, 0, 0))
                    }
                }

                // Financial Bar Chart Section
                viewModel.keuanganChartData?.let { chartData ->
                    item {
                        KeuanganBarChart(chartData)
                    }
                }

                item {
                    Text(text = "Ringkasan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }

                item {
                    DashboardItem(
                        label = "Upcoming Agenda",
                        value = data.upcomingAgenda.toString(),
                        onClick = onNavigateToAgenda
                    )
                }
                item {
                    DashboardItem(
                        label = "Pending Berita",
                        value = data.pendingBerita.toString(),
                        onClick = onNavigateToBerita
                    )
                }
                item {
                    DashboardItem(
                        label = "Pending Artikel",
                        value = data.pendingArtikel.toString(),
                        onClick = onNavigateToArtikel
                    )
                }
                item {
                    DashboardItem(
                        label = "Total Keuangan",
                        value = "Rp ${data.totalKeuangan}",
                        onClick = onNavigateToKeuangan
                    )
                }
            }
        }

        viewModel.errorMessage?.let {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ArtikelStatusPieChart(counts: ArtikelStatusCounts) {
    val total = if (counts.total == 0) 1f else counts.total.toFloat()
    
    val acceptedSweep = (counts.accepted / total) * 360f
    val pendingSweep = (counts.pending / total) * 360f
    val rejectedSweep = (counts.rejected / total) * 360f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Status Artikel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val strokeWidth = 30f
                    
                    // Accepted (Green)
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = acceptedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Pending (Yellow)
                    drawArc(
                        color = Color(0xFFFFC107),
                        startAngle = -90f + acceptedSweep,
                        sweepAngle = pendingSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Rejected (Red)
                    drawArc(
                        color = Color(0xFFF44336),
                        startAngle = -90f + acceptedSweep + pendingSweep,
                        sweepAngle = rejectedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = counts.total.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Total", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Terima", count = counts.accepted)
                LegendItem(color = Color(0xFFFFC107), label = "Pending", count = counts.pending)
                LegendItem(color = Color(0xFFF44336), label = "Tolak", count = counts.rejected)
            }
        }
    }
}

@Composable
fun KeuanganBarChart(data: KeuanganChartResponse) {
    val masukData = data.datasets.find { it.label == "Masuk" }?.data ?: emptyList()
    val keluarData = data.datasets.find { it.label == "Keluar" }?.data ?: emptyList()
    
    val maxVal = (masukData + keluarData).maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Arus Kas (12 Bulan Terakhir)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (data.labels.size * 3f)
                    val spaceBetweenGroups = barWidth
                    
                    data.labels.forEachIndexed { index, _ ->
                        val xOffset = index * (barWidth * 3)
                        
                        val mVal = masukData.getOrNull(index)?.toFloat() ?: 0f
                        val kVal = keluarData.getOrNull(index)?.toFloat() ?: 0f
                        
                        val mHeight = (mVal / maxVal) * size.height
                        val kHeight = (kVal / maxVal) * size.height
                        
                        // Draw Masuk Bar (Green)
                        drawRect(
                            color = Color(0xFF22C55E),
                            topLeft = Offset(xOffset, size.height - mHeight),
                            size = Size(barWidth, mHeight)
                        )
                        
                        // Draw Keluar Bar (Red)
                        drawRect(
                            color = Color(0xFFEF4444),
                            topLeft = Offset(xOffset + barWidth, size.height - kHeight),
                            size = Size(barWidth, kHeight)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ChartLegend(color = Color(0xFF22C55E), label = "Masuk")
                Spacer(modifier = Modifier.width(24.dp))
                ChartLegend(color = Color(0xFFEF4444), label = "Keluar")
            }
        }
    }
}

@Composable
fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, MaterialTheme.shapes.extraSmall))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, MaterialTheme.shapes.extraSmall))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = count.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardItem(label: String, value: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
