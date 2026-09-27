package com.aya.xproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aya.xproject.domain.model.Favorite
import com.aya.xproject.domain.model.FavoriteTab

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val tab: String
)

fun FavoriteEntity.toDomain(): Favorite = Favorite(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    tab = FavoriteTab.valueOf(tab)
)

fun Favorite.toEntity(): FavoriteEntity = FavoriteEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    tab = tab.name
)
