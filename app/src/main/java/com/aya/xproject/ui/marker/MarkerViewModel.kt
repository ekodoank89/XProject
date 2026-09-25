package com.aya.xproject.ui.marker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aya.xproject.data.repository.ManualMarkerRepository
import com.aya.xproject.domain.model.ManualMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class MarkerFormState(
    val editingId: Long? = null,
    val name: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val nameError: String? = null,
    val latitudeError: String? = null,
    val longitudeError: String? = null
) {
    val isEditing: Boolean get() = editingId != null
}

data class RadiusDialogState(
    val markerId: Long,
    val markerName: String,
    val text: String,
    val error: String? = null
)

@HiltViewModel
class MarkerViewModel @Inject constructor(
    private val manualMarkerRepository: ManualMarkerRepository
) : ViewModel() {

    private val _form = MutableStateFlow(MarkerFormState())
    val form: StateFlow<MarkerFormState> = _form.asStateFlow()

    private val _radiusDialog = MutableStateFlow<RadiusDialogState?>(null)
    val radiusDialog: StateFlow<RadiusDialogState?> = _radiusDialog.asStateFlow()

    val markers: StateFlow<List<ManualMarker>> = manualMarkerRepository.markers

    fun onNameChanged(value: String) =
        _form.update { it.copy(name = value, nameError = null) }

    fun onLatitudeChanged(value: String) =
        _form.update { it.copy(latitude = value, latitudeError = null) }

    fun onLongitudeChanged(value: String) =
        _form.update { it.copy(longitude = value, longitudeError = null) }

    fun startEdit(marker: ManualMarker) {
        _form.value = MarkerFormState(
            editingId = marker.id,
            name = marker.name,
            latitude = formatCoordinate(marker.latitude),
            longitude = formatCoordinate(marker.longitude)
        )
    }

    fun cancelEdit() {
        _form.value = MarkerFormState()
    }

    fun save() {
        val form = _form.value
        val nameError = if (form.name.isBlank()) "Nama wajib diisi" else null
        val (lat, latErr) = parseCoordinate(form.latitude, -90.0, 90.0, "Latitude")
        val (lng, lngErr) = parseCoordinate(form.longitude, -180.0, 180.0, "Longitude")

        if (nameError != null || latErr != null || lngErr != null) {
            _form.update {
                it.copy(nameError = nameError, latitudeError = latErr, longitudeError = lngErr)
            }
            return
        }

        val editingId = form.editingId
        // Radius lama dipertahankan saat edit (radius diubah lewat tombol Radius di list)
        val oldRadius = editingId
            ?.let { id -> markers.value.firstOrNull { it.id == id }?.radiusMeters }
            ?: 0.0

        val marker = ManualMarker(
            id = editingId ?: manualMarkerRepository.nextId(),
            name = form.name.trim(),
            latitude = lat,
            longitude = lng,
            radiusMeters = oldRadius
        )

        viewModelScope.launch {
            if (editingId != null) manualMarkerRepository.update(marker)
            else manualMarkerRepository.add(marker)
            cancelEdit()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { manualMarkerRepository.delete(id) }
    }

    // ===== Dialog set radius =====

    fun openRadiusDialog(marker: ManualMarker) {
        _radiusDialog.value = RadiusDialogState(
            markerId = marker.id,
            markerName = marker.name,
            text = String.format(Locale.US, "%.2f", marker.radiusMeters)
        )
    }

    fun onRadiusTextChanged(value: String) {
        _radiusDialog.update { it?.copy(text = value, error = null) }
    }

    fun closeRadiusDialog() {
        _radiusDialog.value = null
    }

    fun saveRadius() {
        val dialog = _radiusDialog.value ?: return
        val value = dialog.text.toDoubleOrNull()
        when {
            value == null || value < 0.0 ->
                _radiusDialog.update { it?.copy(error = "Radius tidak valid (harus ≥ 0)") }

            value > 100_000.0 ->
                _radiusDialog.update { it?.copy(error = "Radius maksimal 100.000 m") }

            else -> {
                manualMarkerRepository.setRadius(dialog.markerId, value)
                _radiusDialog.value = null
            }
        }
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
