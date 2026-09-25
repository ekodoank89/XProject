package com.aya.xproject.domain.model

/**
 * Marker manual untuk pengukuran radius dari sebuah titik koordinat.
 * [radiusMeters] = 0 berarti belum di-set (lingkaran tidak digambar).
 */
data class ManualMarker(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double = 0.0
)
