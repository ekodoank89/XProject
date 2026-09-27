package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.domain.model.JitterSettings
import com.aya.xproject.domain.model.JitterTab
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
 * Sumber kebenaran konfigurasi jitter per tab (GRB & GJK) — PERSISTEN via DataStore.
 * Nilai yang diatur user tetap ada meski app ditutup atau paksa berhenti.
 */
@Singleton
class JitterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settings: StateFlow<Map<JitterTab, JitterSettings>> = dataStore.data
        .map { prefs ->
            JitterTab.entries.associateWith { tab -> readSettings(prefs, tab) }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = JitterTab.entries.associateWith { JitterSettings.defaultFor(it) }
        )

    fun updateSettings(tab: JitterTab, settings: JitterSettings) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[PrefsKeys.jitterStep(tab)] = settings.stepPerWindowMeters
                prefs[PrefsKeys.jitterInterval(tab)] = settings.windowIntervalSeconds
                prefs[PrefsKeys.jitterRadius(tab)] = settings.maxRadiusMeters
            }
        }
    }

    private fun readSettings(prefs: Preferences, tab: JitterTab): JitterSettings {
        val default = JitterSettings.defaultFor(tab)
        return JitterSettings(
            stepPerWindowMeters = prefs[PrefsKeys.jitterStep(tab)] ?: default.stepPerWindowMeters,
            windowIntervalSeconds = prefs[PrefsKeys.jitterInterval(tab)]
                ?: default.windowIntervalSeconds,
            maxRadiusMeters = prefs[PrefsKeys.jitterRadius(tab)] ?: default.maxRadiusMeters
        )
    }
}
