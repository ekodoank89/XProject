package com.aya.xproject.ui.jitter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.repository.JitterRepository
import com.aya.xproject.data.repository.SessionPreferencesRepository
import com.aya.xproject.domain.model.JitterSettings
import com.aya.xproject.domain.model.JitterTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class JitterViewModel @Inject constructor(
    private val jitterRepository: JitterRepository,
    private val sessionPreferences: SessionPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<JitterUiState> = combine(
        jitterRepository.settings,
        sessionPreferences.jitterTab
    ) { settingsMap, tab ->
        JitterUiState(
            selectedTab = tab,
            settings = settingsMap[tab] ?: JitterSettings.defaultFor(tab)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JitterUiState()
    )

    fun selectTab(tab: JitterTab) {
        sessionPreferences.setJitterTab(tab) // tersimpan → dibuka lagi tetap di tab ini
    }

    fun onStepChanged(value: Float) = updateCurrent { it.copy(stepPerWindowMeters = value) }

    fun onIntervalChanged(value: Int) = updateCurrent {
        it.copy(windowIntervalSeconds = value)
    }

    fun onRadiusChanged(value: Float) = updateCurrent { it.copy(maxRadiusMeters = value) }

    /** Reset nilai tab yang sedang aktif ke default-nya. */
    fun resetToDefault() {
        val tab = sessionPreferences.jitterTab.value
        jitterRepository.updateSettings(tab, JitterSettings.defaultFor(tab))
    }

    private fun updateCurrent(transform: (JitterSettings) -> JitterSettings) {
        val tab = sessionPreferences.jitterTab.value
        val current = jitterRepository.settings.value[tab] ?: JitterSettings.defaultFor(tab)
        jitterRepository.updateSettings(tab, transform(current))
    }
}
