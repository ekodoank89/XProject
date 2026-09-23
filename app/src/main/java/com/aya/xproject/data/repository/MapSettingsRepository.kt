package com.aya.xproject.data.repository

import com.aya.xproject.domain.model.MapSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sumber kebenaran preferensi peta, dipakai bersama oleh
 * beberapa screen (Maps, Option, dst).
 * Sementara in-memory; nanti bisa dimigrasi ke DataStore
 * tanpa mengubah ViewModel maupun UI.
 */
@Singleton
class MapSettingsRepository @Inject constructor() {

    private val _settings = MutableStateFlow(MapSettings())
    val settings: StateFlow<MapSettings> = _settings.asStateFlow()

    fun setCoordinateChipVisible(visible: Boolean) {
        _settings.update { it.copy(isCoordinateChipVisible = visible) }
    }
}
