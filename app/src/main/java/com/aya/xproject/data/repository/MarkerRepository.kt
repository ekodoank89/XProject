package com.aya.xproject.data.repository

import com.aya.xproject.domain.model.MapCenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Status marker pelacakan GRB & GJK di peta.
 * null = tidak aktif (marker tidak tampil / telah di-stop).
 */
@Singleton
class MarkerRepository @Inject constructor() {

    private val _grbMarker = MutableStateFlow<MapCenter?>(null)
    val grbMarker: StateFlow<MapCenter?> = _grbMarker.asStateFlow()

    private val _gjkMarker = MutableStateFlow<MapCenter?>(null)
    val gjkMarker: StateFlow<MapCenter?> = _gjkMarker.asStateFlow()

    fun setGrb(center: MapCenter?) {
        _grbMarker.value = center
    }

    fun setGjk(center: MapCenter?) {
        _gjkMarker.value = center
    }
}
