package com.aya.xproject.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class XProjectDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
