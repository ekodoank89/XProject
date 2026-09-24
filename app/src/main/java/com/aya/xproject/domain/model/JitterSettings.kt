package com.aya.xproject.domain.model

enum class JitterTab(val label: String) {
    GRB("GRB"),
    GJK("GJK")
}

/**
 * Konfigurasi jitter untuk satu tab (GRB/GJK).
 * Nilai default berbeda per tab, didefinisikan di [defaultFor].
 */
data class JitterSettings(
    val stepPerWindowMeters: Float,
    val windowIntervalSeconds: Int,
    val maxRadiusMeters: Float
) {
    companion object {
        val GRB_DEFAULT = JitterSettings(
            stepPerWindowMeters = 2f,
            windowIntervalSeconds = 8,
            maxRadiusMeters = 3f
        )
        val GJK_DEFAULT = JitterSettings(
            stepPerWindowMeters = 3f,
            windowIntervalSeconds = 5,
            maxRadiusMeters = 4f
        )

        fun defaultFor(tab: JitterTab): JitterSettings = when (tab) {
            JitterTab.GRB -> GRB_DEFAULT
            JitterTab.GJK -> GJK_DEFAULT
        }
    }
}
