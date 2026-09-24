package com.aya.xproject.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.repository.FavoriteRepository
import com.aya.xproject.data.repository.MapCenterRepository
import com.aya.xproject.data.repository.SessionPreferencesRepository
import com.aya.xproject.domain.model.Favorite
import com.aya.xproject.domain.model.FavoriteTab
import com.aya.xproject.domain.model.JitterTab
import com.aya.xproject.domain.model.MapCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val mapCenterRepository: MapCenterRepository,
    private val sessionPreferences: SessionPreferencesRepository
) : ViewModel() {

    // Bagian UI sementara (accordion + form). Tab terakhir disimpan di sessionPreferences.
    private val _formUi = MutableStateFlow(FavoriteFormUi())

    val uiState: StateFlow<FavoriteUiState> = combine(
        sessionPreferences.favoriteTab,
        _formUi
    ) { tab, formUi ->
        FavoriteUiState(
            selectedTab = tab,
            isFormExpanded = formUi.isFormExpanded,
            form = formUi.form
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoriteUiState()
    )

    val favorites: StateFlow<List<Favorite>> = favoriteRepository.favorites
    val pinCenter: StateFlow<MapCenter> = mapCenterRepository.center

    fun selectTab(tab: FavoriteTab) {
        sessionPreferences.setFavoriteTab(tab) // tersimpan → dibuka lagi tetap di tab ini
        _formUi.value = FavoriteFormUi()
    }

    fun onFromPinSectionClicked() {
        _formUi.update {
            val willExpand = !(it.isFormExpanded && it.form.isFromPin)
            it.copy(
                isFormExpanded = willExpand,
                form = if (willExpand) it.form.copy(isFromPin = true) else it.form
            )
        }
    }

    fun onManualSectionClicked() {
        _formUi.update {
            val willExpand = !(it.isFormExpanded && !it.form.isFromPin)
            it.copy(
                isFormExpanded = willExpand,
                form = if (willExpand) it.form.copy(isFromPin = false) else it.form
            )
        }
    }

    fun onNameChanged(value: String) {
        _formUi.update { it.copy(form = it.form.copy(name = value, nameError = null)) }
    }

    fun onLatitudeChanged(value: String) {
        _formUi.update { it.copy(form = it.form.copy(latitude = value, latitudeError = null)) }
    }

    fun onLongitudeChanged(value: String) {
        _formUi.update { it.copy(form = it.form.copy(longitude = value, longitudeError = null)) }
    }

    fun startEdit(favorite: Favorite) {
        _formUi.value = FavoriteFormUi(
            isFormExpanded = true,
            form = FavoriteFormState(
                isFromPin = false,
                editingId = favorite.id,
                name = favorite.name,
                latitude = formatCoordinate(favorite.latitude),
                longitude = formatCoordinate(favorite.longitude)
            )
        )
    }

    fun cancelEdit() {
        _formUi.value = FavoriteFormUi()
    }

    fun save() {
        val form = _formUi.value.form
        val nameError = if (form.name.isBlank()) "Nama wajib diisi" else null

        var latitude: Double? = null
        var longitude: Double? = null
        var latitudeError: String? = null
        var longitudeError: String? = null

        if (form.isFromPin && !form.isEditing) {
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
            _formUi.update {
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
        val name = form.name.trim()
        val lat = latitude ?: return
        val lon = longitude ?: return

        viewModelScope.launch {
            val tab = editingId
                ?.let { id -> favoriteRepository.getById(id)?.tab }
                ?: sessionPreferences.favoriteTab.value

            val favorite = Favorite(
                id = editingId ?: favoriteRepository.nextId(),
                name = name,
                latitude = lat,
                longitude = lon,
                tab = tab
            )

            if (editingId != null) favoriteRepository.update(favorite) else favoriteRepository.add(favorite)
            cancelEdit()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { favoriteRepository.delete(id) }
    }

    /**
     * Tap nama favorite: pin menuju koordinat, lalu AUTO PLAY sesuai kategori
     * favorit (tab GRB → play GRB, tab GJK → play GJK) dengan pusat jitter
     * di koordinat favorit.
     */
    fun selectFavorite(favorite: Favorite) {
        mapCenterRepository.requestMoveTo(
            MapCenter(favorite.latitude, favorite.longitude),
            playTab = JitterTab.valueOf(favorite.tab.name) // enum paralel: GRB/GJK
        )
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
