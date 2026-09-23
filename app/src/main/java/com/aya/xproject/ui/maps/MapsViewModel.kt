package com.aya.xproject.ui.maps

import androidx.lifecycle.ViewModel
import com.aya.xproject.domain.model.MapCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    fun onMapCenterChanged(center: MapCenter, zoom: Float) {
        _uiState.update { it.copy(center = center, zoom = zoom) }
    }
}
