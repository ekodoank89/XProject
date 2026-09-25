package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.domain.model.ManualMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sumber kebenaran marker manual (pengukuran radius) — PERSISTEN via DataStore.
 * List disimpan sebagai JSON; jumlah marker manual kecil sehingga DataStore memadai
 * (dipilih agar tidak perlu migrasi skema Room).
 */
@Singleton
class ManualMarkerRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val markers: StateFlow<List<ManualMarker>> = dataStore.data
        .map { prefs -> decode(prefs[PrefsKeys.MANUAL_MARKERS] ?: "[]") }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun add(marker: ManualMarker) = write { it + marker }

    fun update(marker: ManualMarker) = write { list ->
        list.map { if (it.id == marker.id) marker else it }
    }

    fun delete(id: Long) = write { list ->
        list.filterNot { it.id == id }
    }

    fun setRadius(id: Long, radiusMeters: Double) = write { list ->
        list.map { if (it.id == id) it.copy(radiusMeters = radiusMeters) else it }
    }

    fun nextId(): Long = System.currentTimeMillis()

    private fun write(transform: (List<ManualMarker>) -> List<ManualMarker>) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = decode(prefs[PrefsKeys.MANUAL_MARKERS] ?: "[]")
                prefs[PrefsKeys.MANUAL_MARKERS] = encode(transform(current))
            }
        }
    }

    private fun decode(json: String): List<ManualMarker> = runCatching {
        val array = JSONArray(json)
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ManualMarker(
                id = item.getLong("id"),
                name = item.getString("name"),
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude"),
                radiusMeters = item.getDouble("radius")
            )
        }
    }.getOrDefault(emptyList())

    private fun encode(markers: List<ManualMarker>): String {
        val array = JSONArray()
        markers.forEach { marker ->
            array.put(
                JSONObject()
                    .put("id", marker.id)
                    .put("name", marker.name)
                    .put("latitude", marker.latitude)
                    .put("longitude", marker.longitude)
                    .put("radius", marker.radiusMeters)
            )
        }
        return array.toString()
    }
}
