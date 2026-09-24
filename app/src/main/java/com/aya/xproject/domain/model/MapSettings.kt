package com.aya.xproject.domain.model

/**
 * Model domain preferensi tampilan peta.
 * Saat ada opsi baru, cukup tambahkan properti di sini.
 */
data class MapSettings(
    val isCoordinateChipVisible: Boolean = true,
    val isGrbChipVisible: Boolean = true,
    val isGjkChipVisible: Boolean = true
)
