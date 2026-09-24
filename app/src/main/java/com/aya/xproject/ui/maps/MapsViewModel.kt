package com.aya.xproject.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.jitter.JitterEngine
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
    private val markerRepository: MarkerRepository,
    jitterEngine: JitterEngine
) : ViewModel() {

    // State dasar: peta, preferensi, marker
    private val baseState = combine(
        mapCenterRepository.snapshot,
        mapCenterRepository.pendingCameraTarget,
        mapSettingsRepository.settings,
        markerRepository.grbMarker,
        markerRepository.gjkMarker
    ) { snap, pendingTarget, settings, grb, gjk ->
        MapsUiState(
            center = snap.center,
            zoom = snap.zoom,
            isCenterLoaded = snap.isLoaded,
            isCoordinateChipVisible = settings.isCoordinateChipVisible,
            isGrbChipVisible = settings.isGrbChipVisible,
            isGjkChipVisible = settings.isGjkChipVisible,
            pendingCameraTarget = pendingTarget,
            grbMarker = grb,
            gjkMarker = gjk
        )
    }

    // Gabungkan dengan posisi jitter yang bergerak live
    val uiState: StateFlow<MapsUiState> = combine(
        baseState,
        jitterEngine.grbPosition,
        jitterEngine.gjkPosition
    ) { state, grbJitter, gjkJitter ->
        state.copy(
            grbJitterPosition = grbJitter,
            gjkJitterPosition = gjkJitter
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

    /** Play/stop GRB: play → marker tersimpan → jitter otomatis jalan. */
    fun toggleGrb(target: MapCenter) {
        val isActive = markerRepository.grbMarker.value != null
        markerRepository.setGrb(if (isActive) null else target)
    }

    /** Play/stop GJK: play → marker tersimpan → jitter otomatis jalan. */
    fun toggleGjk(target: MapCenter) {
        val isActive = markerRepository.gjkMarker.value != null
        markerRepository.setGjk(if (isActive) null else target)
    }

    /** Tap chip koordinat: minta kamera (pin) bergerak ke koordinat target. */
    fun onMoveToRequested(target: MapCenter) {
        mapCenterRepository.requestMoveTo(target)
    }
}
