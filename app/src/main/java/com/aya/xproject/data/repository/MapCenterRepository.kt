package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.domain.model.MapCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Posisi peta + status pemuatan (dipakai untuk restore kamera saat app dibuka). */
data class CenterSnapshot(
    val center: MapCenter,
    val zoom: Float,
    val isLoaded: Boolean
) {
    companion object {
        val INITIAL = CenterSnapshot(MapCenter(-6.2088, 106.8456), 15f, isLoaded = false)
    }
}

/**
 * Sumber kebenaran posisi pin (titik tengah peta) — PERSISTEN via DataStore.
 * Saat app dibuka ulang, kamera langsung mulai di posisi terakhir.
 * pendingCameraTarget tetap in-memory (transien, hanya untuk navigasi sesi aktif).
 */
@Singleton
class MapCenterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _pendingCameraTarget = MutableStateFlow<MapCenter?>(null)
    val pendingCameraTarget: StateFlow<MapCenter?> = _pendingCameraTarget.asStateFlow()

    val snapshot: StateFlow<CenterSnapshot> = dataStore.data
        .map { prefs ->
            val lat = prefs[PrefsKeys.CENTER_LAT]?.toDoubleOrNull()
            val lng = prefs[PrefsKeys.CENTER_LNG]?.toDoubleOrNull()
            CenterSnapshot(
                center = if (lat != null && lng != null) MapCenter(lat, lng)
                else MapCenter(-6.2088, 106.8456),
                zoom = prefs[PrefsKeys.CENTER_ZOOM] ?: 15f,
                isLoaded = true
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, CenterSnapshot.INITIAL)

    val center: StateFlow<MapCenter> = snapshot
        .map { it.center }
        .stateIn(scope, SharingStarted.Eagerly, CenterSnapshot.INITIAL.center)

    fun updateCenter(center: MapCenter, zoom: Float) {
        scope.launch {
            dataStore.edit {
                it[PrefsKeys.CENTER_LAT] = center.latitude.toString()
                it[PrefsKeys.CENTER_LNG] = center.longitude.toString()
                it[PrefsKeys.CENTER_ZOOM] = zoom
            }
        }
    }

    fun requestMoveTo(target: MapCenter) {
        _pendingCameraTarget.value = target
    }

    fun consumePendingTarget() {
        _pendingCameraTarget.value = null
    }
}
