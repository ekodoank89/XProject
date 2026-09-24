package com.aya.xproject.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.repository.MapCenterRepository
import com.aya.xproject.data.repository.MapSettingsRepository
import com.aya.xproject.data.repository.MarkerRepository
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
    mapSettingsRepository: MapSettingsRepository,
    private val markerRepository: MarkerRepository
) : ViewModel() {

    val uiState: StateFlow<MapsUiState> = combine(
        mapCenterRepository.center,
        mapCenterRepository.pendingCameraTarget,
        mapSettingsRepository.settings,
        markerRepository.grbMarker,
        markerRepository.gjkMarker
    ) { center, pendingTarget, settings, grb, gjk ->
        MapsUiState(
            center = center,
            isCoordinateChipVisible = settings.isCoordinateChipVisible,
            isGrbChipVisible = settings.isGrbChipVisible,
            isGjkChipVisible = settings.isGjkChipVisible,
            pendingCameraTarget = pendingTarget,
            grbMarker = grb,
            gjkMarker = gjk
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

    /** Play/stop GRB: play mengunci posisi pin saat itu, stop menghapus marker. */
    fun toggleGrb() {
        val isActive = markerRepository.grbMarker.value != null
        markerRepository.setGrb(if (isActive) null else mapCenterRepository.center.value)
    }

    /** Play/stop GJK: play mengunci posisi pin saat itu, stop menghapus marker. */
    fun toggleGjk() {
        val isActive = markerRepository.gjkMarker.value != null
        markerRepository.setGjk(if (isActive) null else mapCenterRepository.center.value)
    }

    /** Tap chip koordinat: minta kamera (pin) bergerak ke koordinat marker. */
    fun onMoveToRequested(target: MapCenter) {
        mapCenterRepository.requestMoveTo(target)
    }
}
