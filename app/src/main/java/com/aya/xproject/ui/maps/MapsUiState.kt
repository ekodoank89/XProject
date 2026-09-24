package com.aya.xproject.ui.maps

import com.aya.xproject.domain.model.MapCenter

// Ubah koordinat default (Jakarta) sesuai kebutuhan
data class MapsUiState(
    val center: MapCenter = MapCenter(-6.2088, 106.8456),
    val zoom: Float = 15f,
    val isCoordinateChipVisible: Boolean = true,
    val pendingCameraTarget: MapCenter? = null,
    val grbMarker: MapCenter? = null,
    val gjkMarker: MapCenter? = null
)
