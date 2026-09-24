package com.aya.xproject.data.repository

import com.aya.xproject.domain.model.JitterSettings
import com.aya.xproject.domain.model.JitterTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sumber kebenaran konfigurasi jitter, dipisah per tab (GRB & GJK).
 * Nanti dapat dikonsumsi fitur play GRB/GJK di peta.
 */
@Singleton
class JitterRepository @Inject constructor() {

    private val _settings = MutableStateFlow(
        JitterTab.entries.associateWith { JitterSettings.defaultFor(it) }
    )
    val settings: StateFlow<Map<JitterTab, JitterSettings>> = _settings.asStateFlow()

    fun updateSettings(tab: JitterTab, settings: JitterSettings) {
        _settings.update { it + (tab to settings) }
    }
}
