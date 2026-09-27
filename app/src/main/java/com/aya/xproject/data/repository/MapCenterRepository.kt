package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.domain.model.JitterTab
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

/**
 * Permintaan perpindahan kamera.
 * [playTab] non-null = setelah kamera sampai di [target], kategori tersebut
 * di-play otomatis (marker dipasang di target → jitter ikut aktif).
 */
data class CameraRequest(
    val target: MapCenter,
    val playTab: JitterTab? = null
)

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

    private val _pendingCameraTarget = MutableStateFlow<CameraRequest?>(null)
    val pendingCameraTarget: StateFlow<CameraRequest?> = _pendingCameraTarget.asStateFlow()

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

    fun requestMoveTo(target: MapCenter, playTab: JitterTab? = null) {
        _pendingCameraTarget.value = CameraRequest(target, playTab)
    }

    /** Ambil permintaan sekaligus kosongkan; mengembalikan permintaan yang dikonsumsi. */
    fun consumePendingTarget(): CameraRequest? {
        val current = _pendingCameraTarget.value
        _pendingCameraTarget.value = null
        return current
    }
}
