package com.aya.xproject.ui.favorite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.domain.model.Favorite
import com.aya.xproject.domain.model.FavoriteTab
import java.util.Locale

@Composable
fun FavoriteScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allFavorites by viewModel.favorites.collectAsStateWithLifecycle()
    val pinCenter by viewModel.pinCenter.collectAsStateWithLifecycle()

    var deleteCandidate by remember { mutableStateOf<Favorite?>(null) }

    val form = uiState.form
    val tabFavorites = allFavorites.filter { it.tab == uiState.selectedTab }

    Column(modifier = modifier.fillMaxSize()) {

        // ===== Tab menu utama: GRB / GJK =====
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            FavoriteTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.label) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===== Sub menu DARI PIN / MANUAL (sembunyi saat edit) =====
            if (!form.isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.isFromPin,
                        onClick = { viewModel.selectFormMode(true) },
                        label = { Text("DARI PIN") }
                    )
                    FilterChip(
                        selected = !form.isFromPin,
                        onClick = { viewModel.selectFormMode(false) },
                        label = { Text("MANUAL") }
                    )
                }
            } else {
                Text(
                    text = "Edit favorite",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ===== Form =====
            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Nama Favorite") },
                isError = form.nameError != null,
                supportingText = form.nameError?.let { error -> { Text(error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (form.isFromPin && !form.isEditing) {
                Text(
                    text = "Koordinat dari pin: " + String.format(
                        Locale.US, "%.6f, %.6f", pinCenter.latitude, pinCenter.longitude
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
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
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (form.isEditing) {
                    TextButton(onClick = viewModel::cancelEdit) { Text("Batal") }
                }
                Button(onClick = viewModel::save) {
                    Text(if (form.isEditing) "Perbarui" else "Simpan")
                }
            }

            HorizontalDivider()

            // ===== List favorite per tab =====
            Text(
                text = "List Favorite — ${uiState.selectedTab.label}",
                style = MaterialTheme.typography.titleSmall
            )

            if (tabFavorites.isEmpty()) {
                Text(
                    text = "Belum ada favorite di tab ${uiState.selectedTab.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tabFavorites.forEach { favorite ->
                    FavoriteListItem(
                        favorite = favorite,
                        onOpen = {
                            viewModel.selectFavorite(favorite)
                            onNavigateToHome()
                        },
                        onEdit = { viewModel.startEdit(favorite) },
                        onDelete = { deleteCandidate = favorite }
                    )
                }
            }
        }
    }

    // ===== Dialog konfirmasi hapus =====
    if (deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Hapus Favorite") },
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

@Composable
private fun FavoriteListItem(
    favorite: Favorite,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpen)
                    .padding(8.dp)
            ) {
                Text(text = favorite.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = String.format(
                        Locale.US, "%.6f, %.6f", favorite.latitude, favorite.longitude
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus")
            }
        }
    }
}
