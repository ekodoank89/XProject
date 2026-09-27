package com.aya.xproject.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.aya.xproject.data.local.PrefsKeys
import com.aya.xproject.domain.model.FavoriteTab
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
 * Preferensi sesi: tab menu terakhir yang dipilih (Favorite & Jitter).
 * Persisten — menu dibuka kembali tepat di tab terakhir.
 */
@Singleton
class SessionPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val favoriteTab: StateFlow<FavoriteTab> = dataStore.data
        .map { prefs -> prefs[PrefsKeys.FAVORITE_TAB]?.toEnumOrNull() ?: FavoriteTab.GRB }
        .stateIn(scope, SharingStarted.Eagerly, FavoriteTab.GRB)

    val jitterTab: StateFlow<JitterTab> = dataStore.data
        .map { prefs -> prefs[PrefsKeys.JITTER_TAB]?.toEnumOrNull() ?: JitterTab.GRB }
        .stateIn(scope, SharingStarted.Eagerly, JitterTab.GRB)

    fun setFavoriteTab(tab: FavoriteTab) {
        scope.launch { dataStore.edit { it[PrefsKeys.FAVORITE_TAB] = tab.name } }
    }

    fun setJitterTab(tab: JitterTab) {
        scope.launch { dataStore.edit { it[PrefsKeys.JITTER_TAB] = tab.name } }
    }

    private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
        runCatching { enumValueOf<T>(this) }.getOrNull()
}
