package com.aya.xproject.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.repository.MapCenterRepository
import com.aya.xproject.data.repository.MapSettingsRepository
import com.aya.xproject.domain.model.MapCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val mapCenterRepository: MapCenterRepository,
    mapSettingsRepository: MapSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<MapsUiState> = combine(
        mapCenterRepository.center,
        mapCenterRepository.pendingCameraTarget,
        mapSettingsRepository.settings
    ) { center, pendingTarget, settings ->
        MapsUiState(
            center = center,
            isCoordinateChipVisible = settings.isCoordinateChipVisible,
            pendingCameraTarget = pendingTarget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapsUiState()
    )

    fun onMapCenterChanged(center: MapCenter, zoom: Float) {
        mapCenterRepository.updateCenter(center, zoom)
    }

    fun onCameraTargetConsumed() {
        mapCenterRepository.consumePendingTarget()
    }
}
