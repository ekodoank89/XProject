package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.domain.model.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sumber kebenaran preferensi peta — PERSISTEN via DataStore.
 * Setelan tetap ada meski app ditutup atau paksa berhenti.
 */
@Singleton
class MapSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settings: StateFlow<MapSettings> = dataStore.data
        .map { prefs ->
            MapSettings(
                isCoordinateChipVisible = prefs[PrefsKeys.CHIP_COORDINATE] ?: true,
                isGrbChipVisible = prefs[PrefsKeys.CHIP_GRB] ?: true,
                isGjkChipVisible = prefs[PrefsKeys.CHIP_GJK] ?: true,
                isGrbJitterChipVisible = prefs[PrefsKeys.CHIP_GRB_JITTER] ?: true,
                isGjkJitterChipVisible = prefs[PrefsKeys.CHIP_GJK_JITTER] ?: true,
                isGrbJitterDotVisible = prefs[PrefsKeys.GRB_JITTER_DOT] ?: true,
                isGrbRadiusCircleVisible = prefs[PrefsKeys.GRB_RADIUS_CIRCLE] ?: true,
                isGjkJitterDotVisible = prefs[PrefsKeys.GJK_JITTER_DOT] ?: true,
                isGjkRadiusCircleVisible = prefs[PrefsKeys.GJK_RADIUS_CIRCLE] ?: true,
                isManualMarkerVisible = prefs[PrefsKeys.MANUAL_MARKERS_VISIBLE] ?: true
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, MapSettings())

    fun setCoordinateChipVisible(visible: Boolean) =
        write { it[PrefsKeys.CHIP_COORDINATE] = visible }

    fun setGrbChipVisible(visible: Boolean) =
        write { it[PrefsKeys.CHIP_GRB] = visible }

    fun setGjkChipVisible(visible: Boolean) =
        write { it[PrefsKeys.CHIP_GJK] = visible }

    fun setGrbJitterChipVisible(visible: Boolean) =
        write { it[PrefsKeys.CHIP_GRB_JITTER] = visible }

    fun setGjkJitterChipVisible(visible: Boolean) =
        write { it[PrefsKeys.CHIP_GJK_JITTER] = visible }

    fun setGrbJitterDotVisible(visible: Boolean) =
        write { it[PrefsKeys.GRB_JITTER_DOT] = visible }

    fun setGrbRadiusCircleVisible(visible: Boolean) =
        write { it[PrefsKeys.GRB_RADIUS_CIRCLE] = visible }

    fun setGjkJitterDotVisible(visible: Boolean) =
        write { it[PrefsKeys.GJK_JITTER_DOT] = visible }

    fun setGjkRadiusCircleVisible(visible: Boolean) =
        write { it[PrefsKeys.GJK_RADIUS_CIRCLE] = visible }

    fun setManualMarkerVisible(visible: Boolean) =
        write { it[PrefsKeys.MANUAL_MARKERS_VISIBLE] = visible }

    private fun write(block: (MutablePreferences) -> Unit) {
        scope.launch { dataStore.edit(block) }
    }
}