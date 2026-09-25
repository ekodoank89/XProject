package com.aya.xproject.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aya.xproject.domain.model.JitterTab
import com.aya.xproject.domain.model.MapCenter

/** Kunci penyimpanan DataStore + util konversi koordinat. */
object PrefsKeys {
    val CHIP_COORDINATE = booleanPreferencesKey("chip_coordinate_visible")
    val CHIP_GRB = booleanPreferencesKey("chip_grb_visible")
    val CHIP_GJK = booleanPreferencesKey("chip_gjk_visible")
    val CHIP_GRB_JITTER = booleanPreferencesKey("chip_grb_jitter_visible")
    val CHIP_GJK_JITTER = booleanPreferencesKey("chip_gjk_jitter_visible")

    // Visual peta: titik jitter & lingkaran radius
    val GRB_JITTER_DOT = booleanPreferencesKey("grb_jitter_dot_visible")
    val GRB_RADIUS_CIRCLE = booleanPreferencesKey("grb_radius_circle_visible")
    val GJK_JITTER_DOT = booleanPreferencesKey("gjk_jitter_dot_visible")
    val GJK_RADIUS_CIRCLE = booleanPreferencesKey("gjk_radius_circle_visible")

    // Visual peta: list marker manual (gold)
    val MANUAL_MARKERS_VISIBLE = booleanPreferencesKey("manual_markers_visible")

    val CENTER_LAT = stringPreferencesKey("center_lat")
    val CENTER_LNG = stringPreferencesKey("center_lng")
    val CENTER_ZOOM = floatPreferencesKey("center_zoom")

    val MARKER_GRB = stringPreferencesKey("marker_grb")
    val MARKER_GJK = stringPreferencesKey("marker_gjk")

    // Marker manual (pengukuran radius) — list disimpan sebagai JSON
    val MANUAL_MARKERS = stringPreferencesKey("manual_markers_json")

    val FAVORITE_TAB = stringPreferencesKey("favorite_selected_tab")
    val JITTER_TAB = stringPreferencesKey("jitter_selected_tab")

    private val jitterStepKeys = JitterTab.entries.associateWith {
        floatPreferencesKey("jitter_${it.name}_step")
    }
    private val jitterIntervalKeys = JitterTab.entries.associateWith {
        intPreferencesKey("jitter_${it.name}_interval")
    }
    private val jitterRadiusKeys = JitterTab.entries.associateWith {
        floatPreferencesKey("jitter_${it.name}_radius")
    }

    fun jitterStep(tab: JitterTab) = jitterStepKeys.getValue(tab)
    fun jitterInterval(tab: JitterTab) = jitterIntervalKeys.getValue(tab)
    fun jitterRadius(tab: JitterTab) = jitterRadiusKeys.getValue(tab)
}

/** Encode/decode koordinat ke String — Double.toString() round-trip persis. */
fun MapCenter.toPrefsString(): String = "$latitude,$longitude"

fun String.toMapCenterOrNull(): MapCenter? {
    val parts = split(',')
    if (parts.size != 2) return null
    val lat = parts[0].toDoubleOrNull() ?: return null
    val lng = parts[1].toDoubleOrNull() ?: return null
    return MapCenter(lat, lng)
}