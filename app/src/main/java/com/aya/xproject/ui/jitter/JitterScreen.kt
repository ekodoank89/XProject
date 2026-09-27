package com.aya.xproject.ui.jitter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.domain.model.JitterTab
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun JitterScreen(
    modifier: Modifier = Modifier,
    viewModel: JitterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        // ===== Tab menu utama: GRB / GJK =====
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            JitterTab.entries.forEach { tab ->
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
                .padding(16.dp)
        ) {

            Text(
                text = "Pengaturan Jitter — ${uiState.selectedTab.label}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Nilai tersimpan per tab dan berlaku untuk fitur play ${uiState.selectedTab.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ===== Slider: Langkah per jendela (0.5–15 m, kelipatan 0.5) =====
            JitterSliderItem(
                title = "Langkah per jendela",
                valueText = String.format(Locale.US, "%.1f meter", settings.stepPerWindowMeters),
                value = settings.stepPerWindowMeters,
                onValueChange = viewModel::onStepChanged,
                valueRange = 0.5f..15f,
                // (15 - 0.5) / 0.5 - 1 = 28 titik antara ujung slider
                steps = 28
            )

            // ===== Slider: Jendela/interval (1–15 dtk, kelipatan 1) =====
            JitterSliderItem(
                title = "Jendela (interval)",
                valueText = "${settings.windowIntervalSeconds} detik",
                value = settings.windowIntervalSeconds.toFloat(),
                onValueChange = { viewModel.onIntervalChanged(it.roundToInt()) },
                valueRange = 1f..15f,
                // (15 - 1) / 1 - 1 = 13 titik antara ujung slider
                steps = 13
            )

            // ===== Slider: Radius maksimal (1–30 m, kelipatan 0.5) =====
            JitterSliderItem(
                title = "Radius maksimal",
                valueText = String.format(Locale.US, "%.1f meter", settings.maxRadiusMeters),
                value = settings.maxRadiusMeters,
                onValueChange = viewModel::onRadiusChanged,
                valueRange = 1f..30f,
                // (30 - 1) / 0.5 - 1 = 57 titik antara ujung slider
                steps = 57
            )

            // ===== Tombol Default (untuk tab yang sedang aktif) =====
            Button(
                onClick = viewModel::resetToDefault,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Default ${uiState.selectedTab.label}")
            }
        }
    }
}

@Composable
private fun JitterSliderItem(
    title: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
