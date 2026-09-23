package com.aya.xproject.domain.model

/**
 * Model domain titik tengah peta.
 * Domain sengaja tidak bergantung pada LatLng dari Google Maps,
 * agar logika bisnis tetap murni dan mudah dites.
 */
data class MapCenter(
    val latitude: Double,
    val longitude: Double
)
