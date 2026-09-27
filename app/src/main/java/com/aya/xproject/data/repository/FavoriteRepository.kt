package com.aya.xproject.data.repository

import com.aya.xproject.data.local.FavoriteDao
import com.aya.xproject.data.local.toDomain
import com.aya.xproject.data.local.toEntity
import com.aya.xproject.domain.model.Favorite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sumber kebenaran data favorite. Kini didukung database Room —
 * UI dan ViewModel tidak perlu tahu detail penyimpanannya.
 */
@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    // Scope khusus repository untuk mengubah Flow database menjadi StateFlow
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val favorites: StateFlow<List<Favorite>> = favoriteDao.observeAll()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun nextId(): Long = System.currentTimeMillis()

    suspend fun add(favorite: Favorite) = favoriteDao.insert(favorite.toEntity())

    suspend fun update(favorite: Favorite) = favoriteDao.update(favorite.toEntity())

    suspend fun delete(id: Long) = favoriteDao.deleteById(id)

    suspend fun getById(id: Long): Favorite? = favoriteDao.getById(id)?.toDomain()

    /** Untuk export: snapshot seluruh data saat ini. */
    suspend fun getAllOnce(): List<Favorite> = favoriteDao.getAllOnce().map { it.toDomain() }

    /** Untuk import: mengganti seluruh isi database secara transaksional. */
    suspend fun replaceAll(favorites: List<Favorite>) {
        favoriteDao.clearAndInsertAll(favorites.map { it.toEntity() })
    }
}
