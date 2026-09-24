package com.aya.xproject.data.repository

import com.aya.xproject.domain.model.MapCenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sumber kebenaran posisi pin (titik tengah peta).
 * - Dibaca form "DARI PIN" saat menyimpan favorite.
 * - Menampung permintaan pindah kamera dari layar lain (mis. Favorite).
 */
@Singleton
class MapCenterRepository @Inject constructor() {

    private val _center = MutableStateFlow(MapCenter(-6.2088, 106.8456))
    val center: StateFlow<MapCenter> = _center.asStateFlow()

    private var zoom: Float = 15f

    private val _pendingCameraTarget = MutableStateFlow<MapCenter?>(null)
    val pendingCameraTarget: StateFlow<MapCenter?> = _pendingCameraTarget.asStateFlow()

    fun updateCenter(center: MapCenter, zoom: Float) {
        _center.value = center
        this.zoom = zoom
    }

    fun requestMoveTo(target: MapCenter) {
        _pendingCameraTarget.value = target
    }

    fun consumePendingTarget() {
        _pendingCameraTarget.value = null
    }
}
