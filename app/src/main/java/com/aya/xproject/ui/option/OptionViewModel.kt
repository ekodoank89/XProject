package com.aya.xproject.ui.option

import androidx.lifecycle.ViewModel
import com.aya.xproject.data.repository.MapSettingsRepository
import com.aya.xproject.domain.model.MapSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OptionViewModel @Inject constructor(
    private val mapSettingsRepository: MapSettingsRepository
) : ViewModel() {

    val settings: StateFlow<MapSettings> = mapSettingsRepository.settings

    fun onCoordinateChipVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setCoordinateChipVisible(visible)
    }
}
