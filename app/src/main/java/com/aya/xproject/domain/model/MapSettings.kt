package com.aya.xproject.domain.model

/**
 * Model domain preferensi tampilan peta.
 * Saat ada opsi baru, cukup tambahkan properti di sini.
 */
data class MapSettings(
    val isCoordinateChipVisible: Boolean = true,
    val isGrbChipVisible: Boolean = true,
    val isGjkChipVisible: Boolean = true,
    val isGrbJitterChipVisible: Boolean = true,
    val isGjkJitterChipVisible: Boolean = true,
    // Visual peta: titik jitter & lingkaran radius
    val isGrbJitterDotVisible: Boolean = true,
    val isGrbRadiusCircleVisible: Boolean = true,
    val isGjkJitterDotVisible: Boolean = true,
    val isGjkRadiusCircleVisible: Boolean = true
)