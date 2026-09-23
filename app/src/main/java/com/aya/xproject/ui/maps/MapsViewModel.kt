package com.aya.xproject.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.repository.MapSettingsRepository
import com.aya.xproject.domain.model.MapCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    mapSettingsRepository: MapSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    init {
        // Ikuti perubahan preferensi yang diubah lewat menu OPT
        viewModelScope.launch {
            mapSettingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(isCoordinateChipVisible = settings.isCoordinateChipVisible)
                }
            }
        }
    }

    fun onMapCenterChanged(center: MapCenter, zoom: Float) {
        _uiState.update { it.copy(center = center, zoom = zoom) }
    }
}
