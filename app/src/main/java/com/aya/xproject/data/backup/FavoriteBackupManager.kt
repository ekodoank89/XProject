package com.aya.xproject.data.backup

import android.content.Context
import android.net.Uri
import com.aya.xproject.data.local.FavoriteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportBackup(uri: Uri, favorites: List<FavoriteEntity>): Boolean {
        return try {
            val json = FavoriteBackupCodec.encode(favorites)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importBackup(uri: Uri): List<FavoriteEntity> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: return emptyList()
            FavoriteBackupCodec.decode(json)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
