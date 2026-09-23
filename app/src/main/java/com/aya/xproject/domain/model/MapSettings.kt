package com.aya.xproject.domain.model

/**
 * Model domain preferensi tampilan peta.
 * Saat ada opsi baru, cukup tambahkan properti di sini.
 */
data class MapSettings(
    val isCoordinateChipVisible: Boolean = true
)
