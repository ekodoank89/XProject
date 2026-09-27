package com.aya.xproject.domain.model

enum class FavoriteTab(val label: String) {
    GRB("GRB"),
    GJK("GJK")
}

data class Favorite(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val tab: FavoriteTab
)
