package com.aya.xproject.ui.option

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.domain.model.ManualMarker
import com.aya.xproject.domain.model.MapSettings
import com.aya.xproject.ui.marker.MarkerViewModel
import java.util.Locale

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

            OptionTab.Marker -> MarkerTabContent(
                viewModel = hiltViewModel()
            )

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

        // ===== Section: Visual peta (titik jitter, lingkaran radius, marker manual) =====
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

        OptionSwitchItem(
            title = "List marker",
            subtitle = "Tampilkan/sembunyikan list marker manual (gold) di peta",
            checked = settings.isManualMarkerVisible,
            onCheckedChange = { viewModel.onManualMarkerVisibilityChanged(it) }
        )
    }
}

// ==================== TAB 2: MARKER ====================

/**
 * Tab Marker: sub menu Manual (default hide), form input marker,
 * dan list marker dengan set radius, edit, dan hapus.
 */
@Composable
private fun MarkerTabContent(
    viewModel: MarkerViewModel
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val markers by viewModel.markers.collectAsStateWithLifecycle()
    val radiusDialog by viewModel.radiusDialog.collectAsStateWithLifecycle()

    var isManualExpanded by rememberSaveable { mutableStateOf(false) } // default hide
    var deleteCandidate by remember { mutableStateOf<ManualMarker?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // ===== Sub menu Manual (tap untuk buka/tutup) =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FilterChip(
                selected = isManualExpanded,
                onClick = { isManualExpanded = !isManualExpanded },
                label = { Text("Manual") }
            )
        }

        // ===== Isi sub menu Manual =====
        AnimatedVisibility(visible = isManualExpanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (form.isEditing) {
                    Text(
                        text = "Edit marker",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = form.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Nama Marker") },
                    isError = form.nameError != null,
                    supportingText = form.nameError?.let { error -> { Text(error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = form.latitude,
                    onValueChange = viewModel::onLatitudeChanged,
                    label = { Text("Latitude") },
                    isError = form.latitudeError != null,
                    supportingText = form.latitudeError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = form.longitude,
                    onValueChange = viewModel::onLongitudeChanged,
                    label = { Text("Longitude") },
                    isError = form.longitudeError != null,
                    supportingText = form.longitudeError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (form.isEditing) {
                        TextButton(onClick = viewModel::cancelEdit) { Text("Batal") }
                    }
                    Button(onClick = viewModel::save) {
                        Text(if (form.isEditing) "Perbarui" else "Simpan")
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ===== List Marker =====
        Text(
            text = "List Marker",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (markers.isEmpty()) {
            Text(
                text = "Belum ada marker. Buka sub menu Manual untuk menambah.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                markers.forEach { marker ->
                    ManualMarkerItem(
                        marker = marker,
                        onSetRadius = { viewModel.openRadiusDialog(marker) },
                        onEdit = {
                            isManualExpanded = true // buka form saat edit
                            viewModel.startEdit(marker)
                        },
                        onDelete = { deleteCandidate = marker }
                    )
                }
            }
        }
    }

    // ===== Dialog set radius =====
    radiusDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = viewModel::closeRadiusDialog,
            title = { Text("Set Radius") },
            text = {
                Column {
                    Text(
                        text = "Radius pengukuran untuk \"${dialog.markerName}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = dialog.text,
                        onValueChange = viewModel::onRadiusTextChanged,
                        label = { Text("Radius (meter)") },
                        isError = dialog.error != null,
                        supportingText = dialog.error?.let { error -> { Text(error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveRadius) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeRadiusDialog) { Text("Batal") }
            }
        )
    }

    // ===== Dialog konfirmasi hapus =====
    if (deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Marker") },
            text = { Text("Yakin ingin menghapus \"${deleteCandidate?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteCandidate?.let { viewModel.delete(it.id) }
                        deleteCandidate = null
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Batal") }
            }
        )
    }
}

/** Item list marker manual: nama, koordinat, tombol radius, edit, hapus. */
@Composable
private fun ManualMarkerItem(
    marker: ManualMarker,
    onSetRadius: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = marker.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = String.format(
                    Locale.US,
                    "%.6f, %.6f",
                    marker.latitude,
                    marker.longitude
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol set radius: tampil "0.00 m" sebelum di-set, tap untuk mengubah
                TextButton(onClick = onSetRadius) {
                    Text(
                        text = "Radius: " + String.format(Locale.US, "%.2f m", marker.radiusMeters),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus")
                }
            }
        }
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
