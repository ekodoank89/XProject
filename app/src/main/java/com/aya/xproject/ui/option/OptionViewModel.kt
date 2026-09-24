package com.aya.xproject.ui.option

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.backup.FavoriteBackupManager
import com.aya.xproject.data.repository.MapSettingsRepository
import com.aya.xproject.domain.model.MapSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptionViewModel @Inject constructor(
    private val mapSettingsRepository: MapSettingsRepository,
    private val favoriteBackupManager: FavoriteBackupManager
) : ViewModel() {

    val settings: StateFlow<MapSettings> = mapSettingsRepository.settings

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun onCoordinateChipVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setCoordinateChipVisible(visible)
    }

    fun onGrbChipVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGrbChipVisible(visible)
    }

    fun onGjkChipVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGjkChipVisible(visible)
    }

    fun onGrbJitterChipVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGrbJitterChipVisible(visible)
    }

    fun onGjkJitterChipVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGjkJitterChipVisible(visible)
    }

    fun onGrbJitterDotVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGrbJitterDotVisible(visible)
    }

    fun onGrbRadiusCircleVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGrbRadiusCircleVisible(visible)
    }

    fun onGjkJitterDotVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGjkJitterDotVisible(visible)
    }

    fun onGjkRadiusCircleVisibilityChanged(visible: Boolean) {
        mapSettingsRepository.setGjkRadiusCircleVisible(visible)
    }

    fun exportFavorites(uri: Uri) {
        viewModelScope.launch {
            favoriteBackupManager.export(uri)
                .onSuccess { count ->
                    _statusMessage.value = "Export berhasil: $count data favorite tersimpan"
                }
                .onFailure { e ->
                    _statusMessage.value = "Export gagal: ${e.message ?: e.javaClass.simpleName}"
                }
        }
    }

    fun importFavorites(uri: Uri) {
        viewModelScope.launch {
            favoriteBackupManager.import(uri)
                .onSuccess { count ->
                    _statusMessage.value = "Import berhasil: $count data favorite dimuat"
                }
                .onFailure { e ->
                    _statusMessage.value = "Import gagal: ${e.message ?: e.javaClass.simpleName}"
                }
        }
    }
}