package com.example.gramakhata.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gramakhata.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gramakhata.GramaKhataApplication
import com.example.gramakhata.ui.viewmodel.ReportsViewModel
import com.example.gramakhata.ui.viewmodel.ReportsViewModelFactory
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.Intent
import com.example.gramakhata.ui.viewmodel.ExportStatus

import com.example.gramakhata.ui.viewmodel.ReportPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModelFactory(
            (navController.context.applicationContext as GramaKhataApplication).repository
        )
    )
) {
    val totalGive by viewModel.totalGive.collectAsStateWithLifecycle()
    val totalGet by viewModel.totalGet.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(exportStatus) {
        // ... (previous logic)
        when (val status = exportStatus) {
            is ExportStatus.Success -> {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    status.file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_to_excel)))
                viewModel.resetExportStatus()
            }
            is ExportStatus.Error -> {
                android.widget.Toast.makeText(context, status.message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetExportStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.business_insights), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (exportStatus is ExportStatus.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        IconButton(onClick = { viewModel.exportToExcel(context) }) {
                            Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.export_to_excel))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { viewModel.onPeriodSelected(it) }
            )
            
            val overallGive by viewModel.overallGive.collectAsStateWithLifecycle()
            val overallGet by viewModel.overallGet.collectAsStateWithLifecycle()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ReportSummaryCard(title = stringResource(R.string.financial_overview), giveAmount = totalGive, takeAmount = totalGet)
                }
                
                item {
                    Text(stringResource(R.string.cash_flow_analysis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (totalGive == 0.0 && totalGet == 0.0) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_data_available), style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        GiveGetChart(totalGive, totalGet)
                    }
                }
                
                item {
                    Text(stringResource(R.string.dues_distribution), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (totalGive == 0.0 && totalGet == 0.0) {
                        Text(stringResource(R.string.no_pending_dues), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        DistributionSection(totalGive, totalGet)
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit
) {
    val periods = listOf(
        ReportPeriod.DAILY to stringResource(R.string.daily),
        ReportPeriod.WEEKLY to stringResource(R.string.weekly),
        ReportPeriod.MONTHLY to stringResource(R.string.monthly),
        ReportPeriod.ALL to stringResource(R.string.all_time)
    )

    TabRow(
        selectedTabIndex = periods.indexOfFirst { it.first == selectedPeriod },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[periods.indexOfFirst { it.first == selectedPeriod }]),
                color = MaterialTheme.colorScheme.primary
            )
        },
        divider = {}
    ) {
        periods.forEach { (period, label) ->
            Tab(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun DistributionSection(totalGive: Double, totalGet: Double) {
    val total = totalGive + totalGet
    val giveProgress = if (total > 0) (totalGive / total).toFloat() else 0f
    val getProgress = if (total > 0) (totalGet / total).toFloat() else 0f
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DistributionRow(stringResource(R.string.total_give_label), giveProgress, Color(0xFFDC2626))
        DistributionRow(stringResource(R.string.total_take_label), getProgress, Color(0xFF3B82F6))
    }
}

@Composable
fun DistributionRow(label: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun GiveGetChart(totalGive: Double, totalGet: Double) {
    val max = maxOf(totalGive, totalGet, 1.0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight((totalGive / max).toFloat().coerceIn(0.05f, 1f))
                    .background(Color(0xFFDC2626), MaterialTheme.shapes.extraSmall)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.total_give_label), style = MaterialTheme.typography.labelSmall)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight((totalGet / max).toFloat().coerceIn(0.05f, 1f))
                    .background(Color(0xFF3B82F6), MaterialTheme.shapes.extraSmall)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.total_take_label), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ReportSummaryCard(title: String, giveAmount: Double, takeAmount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.total_give_label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text("₹${giveAmount.toInt()}", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.total_take_label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text("₹${takeAmount.toInt()}", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF85F8C4), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
