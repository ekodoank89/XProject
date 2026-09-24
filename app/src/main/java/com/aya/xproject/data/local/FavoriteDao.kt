package com.aya.xproject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY id ASC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getById(id: Long): FavoriteEntity?

    @Query("SELECT * FROM favorites ORDER BY id ASC")
    suspend fun getAllOnce(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Update
    suspend fun update(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FavoriteEntity>)

    /** Ganti seluruh isi tabel dalam satu transaksi — dipakai untuk import. */
    @Transaction
    suspend fun clearAndInsertAll(entities: List<FavoriteEntity>) {
        clearAll()
        insertAll(entities)
    }
}
