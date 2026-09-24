package com.aya.xproject.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.domain.model.MapCenter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.util.Locale

/** Zoom maksimal yang didukung Google Maps. */
private const val MAX_ZOOM = 21f

/** Durasi animasi kamera untuk tombol kontrol (ms). */
private const val CAMERA_ANIMATION_MS = 500

/** Ukuran sisi tombol play/stop — persegi (1:1). */
private val TrackButtonSize = 80.dp

// Warna aksen GRB (hijau) dan GJK (merah).
// Dipakai BERSAMA oleh chip koordinat dan marker — warna pasti identik.
private val GrbGreen = Color(0xFF2E7D32)
private val GjkRed = Color(0xFFC62828)

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ⏳ Tunggu posisi tersimpan dimuat dari DataStore agar kamera
    // langsung mulai di posisi terakhir (bukan koordinat default).
    if (!uiState.isCenterLoaded) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Klien lokasi untuk fitur auto focus (fokus ke titik biru)
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // ===== Titik biru lokasi (my location) =====
    var isMyLocationEnabled by remember {
        mutableStateOf(hasFineLocationPermission(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isMyLocationEnabled = hasFineLocationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(uiState.center.latitude, uiState.center.longitude),
            uiState.zoom
        )
    }

    /** Posisi kamera LIVE saat ini — sumber kebenaran untuk aksi play. */
    fun currentMapCenter(): MapCenter {
        val target = cameraPositionState.position.target
        return MapCenter(target.latitude, target.longitude)
    }

    // Peta berhenti bergerak → simpan posisi pin (titik tengah) ke penyimpanan permanen
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onMapCenterChanged(
                currentMapCenter(),
                cameraPositionState.position.zoom
            )
        }
    }

    // Ada permintaan pindah kamera (dari Favorite / chip GRB/GJK)
    // → LOMPAT instan ke koordinat, TANPA animasi.
    LaunchedEffect(uiState.pendingCameraTarget) {
        val target = uiState.pendingCameraTarget ?: return@LaunchedEffect
        val zoom = cameraPositionState.position.zoom
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(target.latitude, target.longitude),
            zoom
        )
        viewModel.onCameraTargetConsumed()
        // Simpan posisi final secara eksplisit — jump instan tidak selalu
        // memicu event isMoving, jadi jangan bergantung padanya.
        viewModel.onMapCenterChanged(target, zoom)
    }

    // ===== Aksi tombol kontrol peta =====

    /** Auto focus: kamera ke lokasi GPS pengguna + kompas reset ke utara.
     *  Jika lokasi belum tersedia (GPS belum fix), kompas tetap direset. */
    @SuppressLint("MissingPermission") // izin dicek manual di baris pertama
    fun autoFocus() {
        if (!hasFineLocationPermission(context)) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            scope.launch {
                val current = cameraPositionState.position
                val target = if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    current.target
                }
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(target, current.zoom, 0f, 0f)
                    ),
                    CAMERA_ANIMATION_MS
                )
            }
        }
    }

    /** Zoom in: sekali tap langsung ke zoom maksimal. */
    fun zoomInToMax() {
        scope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.zoomTo(MAX_ZOOM),
                CAMERA_ANIMATION_MS
            )
        }
    }

    /** Zoom out: perilaku bawaan Google — turun 1 level zoom per tap. */
    fun zoomOutOneStep() {
        scope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.zoomOut(),
                CAMERA_ANIMATION_MS
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = isMyLocationEnabled
            ),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            // ⚠️ Marker WAJIB berada di dalam content lambda GoogleMap,
            // kalau tidak app akan crash (penyebab FC sebelumnya).

            // ===== Marker GRB: icon pin, warna hijau (sama dengan chip GRB) =====
            uiState.grbMarker?.let { grb ->
                val grbMarkerState = rememberMarkerState(
                    key = "grb:${grb.latitude},${grb.longitude}",
                    position = LatLng(grb.latitude, grb.longitude)
                )
                MarkerComposable(
                    keys = arrayOf(grb.latitude, grb.longitude),
                    state = grbMarkerState,
                    title = "GRB"
                ) {
                    PinIcon(tint = GrbGreen)
                }
            }

            // ===== Marker GJK: icon pin, warna merah (sama dengan chip GJK) =====
            uiState.gjkMarker?.let { gjk ->
                val gjkMarkerState = rememberMarkerState(
                    key = "gjk:${gjk.latitude},${gjk.longitude}",
                    position = LatLng(gjk.latitude, gjk.longitude)
                )
                MarkerComposable(
                    keys = arrayOf(gjk.latitude, gjk.longitude),
                    state = gjkMarkerState,
                    title = "GJK"
                ) {
                    PinIcon(tint = GjkRed)
                }
            }
        }

        // Pin overlay: selalu di tengah layar.
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .offset(y = (-24).dp)
        )

        // ===== Tombol play/stop GRB & GJK: kiri bawah, tersusun vertikal =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrackButton(
                label = "GRB",
                isActive = uiState.grbMarker != null,
                accent = GrbGreen,
                onClick = { viewModel.toggleGrb(currentMapCenter()) }
            )
            TrackButton(
                label = "GJK",
                isActive = uiState.gjkMarker != null,
                accent = GjkRed,
                onClick = { viewModel.toggleGjk(currentMapCenter()) }
            )
        }

        // ===== Tombol kontrol peta: kanan bawah =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MapControlButton(
                icon = Icons.Filled.MyLocation,
                contentDescription = "Auto focus & kompas",
                onClick = { autoFocus() }
            )
            MapControlButton(
                icon = Icons.Filled.ZoomIn,
                contentDescription = "Zoom maksimal",
                onClick = { zoomInToMax() }
            )
            MapControlButton(
                icon = Icons.Filled.ZoomOut,
                contentDescription = "Zoom out",
                onClick = { zoomOutOneStep() }
            )
        }

        // ===== Chip koordinat: tengah bawah =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chip koordinat marker GRB — bisa disembunyikan dari menu OPT
            if (uiState.isGrbChipVisible) {
                uiState.grbMarker?.let { grb ->
                    CoordinateChip(
                        label = "GRB",
                        coordinate = grb,
                        accent = GrbGreen,
                        onClick = { viewModel.onMoveToRequested(grb) }
                    )
                }
            }

            // Chip koordinat marker GJK — bisa disembunyikan dari menu OPT
            if (uiState.isGjkChipVisible) {
                uiState.gjkMarker?.let { gjk ->
                    CoordinateChip(
                        label = "GJK",
                        coordinate = gjk,
                        accent = GjkRed,
                        onClick = { viewModel.onMoveToRequested(gjk) }
                    )
                }
            }

            // Chip koordinat pin (mengikuti switch di menu OPT)
            if (uiState.isCoordinateChipVisible) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "%.6f, %.6f",
                            uiState.center.latitude,
                            uiState.center.longitude
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/** Icon pin untuk marker — bentuk & ukuran sama dengan pin overlay tengah layar. */
@Composable
private fun PinIcon(tint: Color) {
    Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(48.dp)
    )
}

/** Cek izin fine location secara runtime. */
private fun hasFineLocationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

/** Tombol play/stop pelacakan GRB/GJK — persegi 1:1, ikon di atas, label di bawah. */
@Composable
private fun TrackButton(
    label: String,
    isActive: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) accent else MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = modifier.size(TrackButtonSize)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = if (isActive) Color.White else accent,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Chip koordinat marker (warna sesuai markernya). Tap: pin menuju koordinat. */
@Composable
private fun CoordinateChip(
    label: String,
    coordinate: MapCenter,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent,
        shadowElevation = 4.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = "$label  " + String.format(
                Locale.US,
                "%.6f, %.6f",
                coordinate.latitude,
                coordinate.longitude
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MapControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = modifier.size(48.dp)
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
