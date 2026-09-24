package com.aya.xproject.data.backup

import android.content.Context
import android.net.Uri
import com.aya.xproject.data.repository.FavoriteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ekspor/impor data favorite ke/dari file melalui Storage Access Framework
 * (file picker bawaan Android, tanpa izin penyimpanan apa pun).
 */
@Singleton
class FavoriteBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteRepository: FavoriteRepository
) {

    /** Menulis seluruh data favorite ke file pada [uri]. Mengembalikan jumlah data. */
    suspend fun export(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val favorites = favoriteRepository.getAllOnce()
            val json = FavoriteBackupCodec.toJson(favorites)
            val output = context.contentResolver.openOutputStream(uri, "wt")
                ?: error("Tidak dapat membuka file tujuan")
            output.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            favorites.size
        }
    }

    /** Membaca file pada [uri] lalu MENGGANTI seluruh isi database. Mengembalikan jumlah data. */
    suspend fun import(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: error("Tidak dapat membaca file sumber")

            val favorites = FavoriteBackupCodec.fromJson(json)
            favoriteRepository.replaceAll(favorites)
            favorites.size
        }
    }
}
