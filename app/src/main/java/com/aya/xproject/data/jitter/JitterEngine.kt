package com.aya.xproject.data.jitter

import com.aya.xproject.data.repository.JitterRepository
import com.aya.xproject.data.repository.MarkerRepository
import com.aya.xproject.domain.model.JitterSettings
import com.aya.xproject.domain.model.JitterTab
import com.aya.xproject.domain.model.MapCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mesin jitter: menggerakkan titik acak di sekitar marker GRB/GJK.
 *
 * - Aktif otomatis saat marker play (termasuk restore setelah app dibuka ulang).
 * - Berhenti otomatis saat marker di-stop.
 * - Pusat jitter = posisi marker; gerakan dibatasi radius maksimal.
 * - Parameter (langkah, interval, radius) dibaca ulang tiap langkah,
 *   sehingga perubahan slider di menu JIT langsung berefek.
 */
@Singleton
class JitterEngine @Inject constructor(
    private val markerRepository: MarkerRepository,
    private val jitterRepository: JitterRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _grbPosition = MutableStateFlow<MapCenter?>(null)
    val grbPosition: StateFlow<MapCenter?> = _grbPosition.asStateFlow()

    private val _gjkPosition = MutableStateFlow<MapCenter?>(null)
    val gjkPosition: StateFlow<MapCenter?> = _gjkPosition.asStateFlow()

    private var grbJob: Job? = null
    private var gjkJob: Job? = null

    init {
        // Marker muncul (play/restore) → jalankan; marker hilang (stop) → hentikan.
        scope.launch {
            markerRepository.grbMarker.collect { marker ->
                if (marker != null) start(JitterTab.GRB, marker, _grbPosition) { grbJob = it }
                else {
                    grbJob?.cancel()
                    _grbPosition.value = null
                }
            }
        }
        scope.launch {
            markerRepository.gjkMarker.collect { marker ->
                if (marker != null) start(JitterTab.GJK, marker, _gjkPosition) { gjkJob = it }
                else {
                    gjkJob?.cancel()
                    _gjkPosition.value = null
                }
            }
        }
    }

    private fun start(
        tab: JitterTab,
        center: MapCenter,
        positionFlow: MutableStateFlow<MapCenter?>,
        register: (Job) -> Unit
    ) {
        register(
            scope.launch {
                positionFlow.value = center // mulai di pusat (marker)
                while (true) {
                    val settings = currentSettings(tab)
                    delay(settings.windowIntervalSeconds * 1000L)
                    val current = positionFlow.value ?: center
                    positionFlow.value = nextPosition(current, center, settings)
                }
            }
        )
    }

    private fun currentSettings(tab: JitterTab): JitterSettings =
        jitterRepository.settings.value[tab] ?: JitterSettings.defaultFor(tab)

    /**
     * Posisi berikutnya: langkah acak sejauh [JitterSettings.stepPerWindowMeters]
     * dengan arah acak; jika keluar radius maksimal dari pusat, di-clamp
     * menempel ke tepi lingkaran radius.
     */
    private fun nextPosition(
        current: MapCenter,
        center: MapCenter,
        settings: JitterSettings
    ): MapCenter {
        val angle = Random.nextDouble(0.0, 2 * PI)
        val dNorth = settings.stepPerWindowMeters * cos(angle)
        val dEast = settings.stepPerWindowMeters * sin(angle)
        val candidate = offset(current, dNorth, dEast)
        val distance = distanceMeters(candidate, center)
        return if (distance <= settings.maxRadiusMeters) {
            candidate
        } else {
            clampToRadius(candidate, center, settings.maxRadiusMeters)
        }
    }

    /** Offset posisi ke utara/timur dalam meter (aproksimasi equirectangular). */
    private fun offset(base: MapCenter, dNorthMeters: Double, dEastMeters: Double): MapCenter {
        val lat = base.latitude + dNorthMeters / METERS_PER_DEGREE_LAT
        val lng = base.longitude +
                dEastMeters / metersPerDegreeLng(base.latitude)
        return MapCenter(lat, lng)
    }

    private fun distanceMeters(a: MapCenter, b: MapCenter): Double {
        val dy = (a.latitude - b.latitude) * METERS_PER_DEGREE_LAT
        val dx = (a.longitude - b.longitude) * metersPerDegreeLng((a.latitude + b.latitude) / 2)
        return sqrt(dx * dx + dy * dy)
    }

    private fun clampToRadius(point: MapCenter, center: MapCenter, radiusMeters: Double): MapCenter {
        val dy = (point.latitude - center.latitude) * METERS_PER_DEGREE_LAT
        val dx = (point.longitude - center.longitude) * metersPerDegreeLng(center.latitude)
        val distance = sqrt(dx * dx + dy * dy)
        if (distance == 0.0) return point
        val scale = radiusMeters / distance
        return offset(center, dy * scale, dx * scale)
    }

    private fun metersPerDegreeLng(latitudeDeg: Double): Double =
        METERS_PER_DEGREE_LAT * cos(Math.toRadians(latitudeDeg))

    private companion object {
        const val METERS_PER_DEGREE_LAT = 111_320.0
    }
}
