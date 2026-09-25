package com.aya.xproject.ui.option

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.domain.model.MapSettings

/** Tab menu pada layar Option. */
private enum class OptionTab(val label: String) {
    Hide("Hide"),
    Marker("Marker"),
    Data("Data")
}

@Composable
fun OptionScreen(
    modifier: Modifier = Modifier,
    viewModel: OptionViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    // Tab terakhir dipilih — rememberSaveable agar tetap terpilih
    // saat berpindah menu bottom-nav lalu kembali ke OPT.
    var selectedTab by rememberSaveable { mutableStateOf(OptionTab.Hide) }

    // Launcher export: Android menampilkan dialog "buat file", lalu uri dikirim ke ViewModel
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) viewModel.exportFavorites(uri) }

    // Launcher import: Android menampilkan file picker, lalu minta konfirmasi
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pendingImportUri = uri }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            text = "Option",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        // ===== Tab menu: Hide / Marker / Data =====
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            OptionTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        when (selectedTab) {
            OptionTab.Hide -> HideTabContent(
                settings = settings,
                viewModel = viewModel
            )

            OptionTab.Marker -> MarkerTabContent()

            OptionTab.Data -> DataTabContent(
                statusMessage = statusMessage,
                onExportClick = { exportLauncher.launch("xproject_favorite_backup.json") },
                onImportClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }
            )
        }
    }

    // Dialog konfirmasi import (karena datanya menimpa) — global, di atas tab
    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Import Favorite") },
            text = { Text("Import akan MENGGANTI seluruh data favorite saat ini dengan isi file. Lanjutkan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri?.let(viewModel::importFavorites)
                        pendingImportUri = null
                    }
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text("Batal") }
            }
        )
    }
}

// ==================== TAB 1: HIDE ====================

/** Seluruh switch on/off — label & fungsi sama persis, hanya dipindah ke tab Hide. */
@Composable
private fun HideTabContent(
    settings: MapSettings,
    viewModel: OptionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ===== Section: Chip koordinat =====
        OptionSwitchItem(
            title = "Chip koordinat",
            subtitle = "Tampilkan/sembunyikan chip koordinat pin di peta",
            checked = settings.isCoordinateChipVisible,
            onCheckedChange = { viewModel.onCoordinateChipVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Chip koordinat GRB",
            subtitle = "Tampilkan/sembunyikan chip koordinat marker GRB (hijau)",
            checked = settings.isGrbChipVisible,
            onCheckedChange = { viewModel.onGrbChipVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Chip koordinat GJK",
            subtitle = "Tampilkan/sembunyikan chip koordinat marker GJK (merah)",
            checked = settings.isGjkChipVisible,
            onCheckedChange = { viewModel.onGjkChipVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Chip koordinat Jitter GRB",
            subtitle = "Tampilkan/sembunyikan chip koordinat jitter GRB (bergerak live)",
            checked = settings.isGrbJitterChipVisible,
            onCheckedChange = { viewModel.onGrbJitterChipVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Chip koordinat Jitter GJK",
            subtitle = "Tampilkan/sembunyikan chip koordinat jitter GJK (bergerak live)",
            checked = settings.isGjkJitterChipVisible,
            onCheckedChange = { viewModel.onGjkJitterChipVisibilityChanged(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ===== Section: Visual peta (titik jitter & lingkaran radius) =====
        OptionSwitchItem(
            title = "Titik jitter GRB",
            subtitle = "Tampilkan/sembunyikan titik bergerak jitter GRB di peta",
            checked = settings.isGrbJitterDotVisible,
            onCheckedChange = { viewModel.onGrbJitterDotVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Radius maksimal GRB",
            subtitle = "Tampilkan/sembunyikan lingkaran radius jitter GRB di peta",
            checked = settings.isGrbRadiusCircleVisible,
            onCheckedChange = { viewModel.onGrbRadiusCircleVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Titik jitter GJK",
            subtitle = "Tampilkan/sembunyikan titik bergerak jitter GJK di peta",
            checked = settings.isGjkJitterDotVisible,
            onCheckedChange = { viewModel.onGjkJitterDotVisibilityChanged(it) }
        )

        OptionSwitchItem(
            title = "Radius maksimal GJK",
            subtitle = "Tampilkan/sembunyikan lingkaran radius jitter GJK di peta",
            checked = settings.isGjkRadiusCircleVisible,
            onCheckedChange = { viewModel.onGjkRadiusCircleVisibilityChanged(it) }
        )
    }
}

// ==================== TAB 2: MARKER ====================

/** Placeholder — siap diisi pengaturan marker pada pengembangan berikutnya. */
@Composable
private fun MarkerTabContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Belum ada pengaturan marker",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== TAB 3: DATA ====================

/** Database Favorite — label, tombol, dan fungsi sama persis, dipindah ke tab Data. */
@Composable
private fun DataTabContent(
    statusMessage: String?,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Database Favorite",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Text(
            text = "Export menyimpan seluruh data favorite (GRB & GJK) ke file. " +
                "Import mengganti data saat ini dengan isi file backup.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onExportClick) {
                Text("Export")
            }
            OutlinedButton(onClick = onImportClick) {
                Text("Import")
            }
        }

        statusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ==================== Komponen bersama ====================

@Composable
private fun OptionSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
