package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.data.local.toMapCenterOrNull
import com.aya.xproject.data.local.toPrefsString
import com.aya.xproject.domain.model.MapCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Status marker pelacakan GRB & GJK — PERSISTEN via DataStore.
 * null = tidak aktif (marker tidak tampil / telah di-stop).
 * Play/stop tersimpan: app dibuka ulang, marker muncul kembali.
 */
@Singleton
class MarkerRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val grbMarker: StateFlow<MapCenter?> = dataStore.data
        .map { it[PrefsKeys.MARKER_GRB]?.toMapCenterOrNull() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val gjkMarker: StateFlow<MapCenter?> = dataStore.data
        .map { it[PrefsKeys.MARKER_GJK]?.toMapCenterOrNull() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun setGrb(center: MapCenter?) = writeMarker(PrefsKeys.MARKER_GRB, center)

    fun setGjk(center: MapCenter?) = writeMarker(PrefsKeys.MARKER_GJK, center)

    private fun writeMarker(key: Preferences.Key<String>, center: MapCenter?) {
        scope.launch {
            dataStore.edit { prefs ->
                if (center == null) prefs.remove(key) else prefs[key] = center.toPrefsString()
            }
        }
    }
}
