package com.aya.xproject.ui.maps

import com.aya.xproject.domain.model.MapCenter

data class MapsUiState(
    val center: MapCenter = MapCenter(-6.2088, 106.8456),
    val zoom: Float = 15f,
    val isCenterLoaded: Boolean = false,
    val isCoordinateChipVisible: Boolean = true,
    val isGrbChipVisible: Boolean = true,
    val isGjkChipVisible: Boolean = true,
    val isGrbJitterChipVisible: Boolean = true,
    val isGjkJitterChipVisible: Boolean = true,
    val pendingCameraTarget: MapCenter? = null,
    val grbMarker: MapCenter? = null,
    val gjkMarker: MapCenter? = null,
    // Posisi jitter yang bergerak (null = jitter tidak aktif)
    val grbJitterPosition: MapCenter? = null,
    val gjkJitterPosition: MapCenter? = null
)
