package com.aya.xproject.ui.favorite

import androidx.lifecycle.ViewModel
import com.aya.xproject.data.repository.FavoriteRepository
import com.aya.xproject.data.repository.MapCenterRepository
import com.aya.xproject.domain.model.Favorite
import com.aya.xproject.domain.model.FavoriteTab
import com.aya.xproject.domain.model.MapCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val mapCenterRepository: MapCenterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    val favorites: StateFlow<List<Favorite>> = favoriteRepository.favorites
    val pinCenter: StateFlow<MapCenter> = mapCenterRepository.center

    // Ganti tab: form di-reset agar tidak ada edit yang menggantung lintas tab
    fun selectTab(tab: FavoriteTab) {
        _uiState.update { it.copy(selectedTab = tab, form = FavoriteFormState()) }
    }

    fun selectFormMode(isFromPin: Boolean) {
        _uiState.update { it.copy(form = it.form.copy(isFromPin = isFromPin)) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(form = it.form.copy(name = value, nameError = null)) }
    }

    fun onLatitudeChanged(value: String) {
        _uiState.update { it.copy(form = it.form.copy(latitude = value, latitudeError = null)) }
    }

    fun onLongitudeChanged(value: String) {
        _uiState.update { it.copy(form = it.form.copy(longitude = value, longitudeError = null)) }
    }

    fun startEdit(favorite: Favorite) {
        _uiState.update {
            it.copy(
                form = FavoriteFormState(
                    isFromPin = false, // saat edit, koordinat diubah lewat input manual
                    editingId = favorite.id,
                    name = favorite.name,
                    latitude = formatCoordinate(favorite.latitude),
                    longitude = formatCoordinate(favorite.longitude)
                )
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(form = FavoriteFormState()) }
    }

    fun save() {
        val form = _uiState.value.form
        val nameError = if (form.name.isBlank()) "Nama wajib diisi" else null

        var latitude: Double? = null
        var longitude: Double? = null
        var latitudeError: String? = null
        var longitudeError: String? = null

        if (form.isFromPin && !form.isEditing) {
            // DARI PIN: ambil posisi pin terkini (titik tengah peta)
            latitude = pinCenter.value.latitude
            longitude = pinCenter.value.longitude
        } else {
            val (lat, latErr) = parseCoordinate(form.latitude, -90.0, 90.0, "Latitude")
            latitude = lat
            latitudeError = latErr
            val (lon, lonErr) = parseCoordinate(form.longitude, -180.0, 180.0, "Longitude")
            longitude = lon
            longitudeError = lonErr
        }

        if (nameError != null || latitudeError != null || longitudeError != null) {
            _uiState.update {
                it.copy(
                    form = it.form.copy(
                        nameError = nameError,
                        latitudeError = latitudeError,
                        longitudeError = longitudeError
                    )
                )
            }
            return
        }

        val editingId = form.editingId
        val favorite = Favorite(
            id = editingId ?: favoriteRepository.nextId(),
            name = form.name.trim(),
            latitude = latitude ?: return,
            longitude = longitude ?: return,
            tab = editingId
                ?.let { id -> favoriteRepository.favorites.value.firstOrNull { it.id == id }?.tab }
                ?: _uiState.value.selectedTab
        )

        if (editingId != null) favoriteRepository.update(favorite) else favoriteRepository.add(favorite)
        cancelEdit()
    }

    fun delete(id: Long) {
        favoriteRepository.delete(id)
    }

    /** Tap nama favorite: minta peta memindahkan pin ke koordinat ini. */
    fun selectFavorite(favorite: Favorite) {
        mapCenterRepository.requestMoveTo(MapCenter(favorite.latitude, favorite.longitude))
    }

    private fun formatCoordinate(value: Double): String =
        String.format(Locale.US, "%.6f", value)

    private fun parseCoordinate(
        input: String,
        min: Double,
        max: Double,
        label: String
    ): Pair<Double, String?> {
        if (input.isBlank()) return Pair(Double.NaN, "$label wajib diisi")
        val value = input.toDoubleOrNull() ?: return Pair(Double.NaN, "$label tidak valid")
        if (value < min || value > max) {
            return Pair(Double.NaN, "$label harus antara ${min.toInt()} s/d ${max.toInt()}")
        }
        return Pair(value, null)
    }
}
