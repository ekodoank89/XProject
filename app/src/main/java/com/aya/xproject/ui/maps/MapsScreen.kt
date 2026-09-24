package com.aya.xproject.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

/** Zoom maksimal yang didukung Google Maps. */
private const val MAX_ZOOM = 21f

/** Durasi animasi kamera untuk tombol kontrol (ms). */
private const val CAMERA_ANIMATION_MS = 500

// Warna aksen GRB (hijau) dan GJK (merah)
private val GrbGreen = Color(0xFF2E7D32)
private val GjkRed = Color(0xFFC62828)

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Klien lokasi untuk fitur auto focus (fokus ke titik biru)
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // ===== Titik biru lokasi (my location) =====
    // Flag hanya boleh true jika izin fine location benar-benar terpenuhi,
    // kalau tidak app akan crash (SecurityException).
    var isMyLocationEnabled by remember {
        mutableStateOf(hasFineLocationPermission(context))
    }

    // Saat kembali ke app, status izin dicek ulang agar titik biru mengikuti kondisi terkini.
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

    // Peta berhenti bergerak → simpan posisi pin (titik tengah) ke repository
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            viewModel.onMapCenterChanged(
                MapCenter(target.latitude, target.longitude),
                cameraPositionState.position.zoom
            )
        }
    }

    // Ada permintaan pindah kamera (dari Favorite / chip GRB/GJK) → animasikan
    LaunchedEffect(uiState.pendingCameraTarget) {
        val target = uiState.pendingCameraTarget ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(target.latitude, target.longitude),
                cameraPositionState.position.zoom
            ),
            700
        )
        viewModel.onCameraTargetConsumed()
    }

    // ===== Aksi tombol kontrol peta =====

    /** Auto focus: animasikan kamera ke lokasi GPS pengguna (titik biru)
     *  + fungsi kompas: reset bearing ke utara dan tilt diratakan. */
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
        )

        // ===== Marker GRB (hijau) — tampil hanya saat play =====
        uiState.grbMarker?.let { grb ->
            Marker(
                state = MarkerState(position = LatLng(grb.latitude, grb.longitude)),
                title = "GRB",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        // ===== Marker GJK (merah) — tampil hanya saat play =====
        uiState.gjkMarker?.let { gjk ->
            Marker(
                state = MarkerState(position = LatLng(gjk.latitude, gjk.longitude)),
                title = "GJK",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
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

        // ===== Tombol play/stop GRB & GJK: kiri bawah =====
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrackButton(
                label = "GRB",
                isActive = uiState.grbMarker != null,
                accent = GrbGreen,
                onClick = viewModel::toggleGrb
            )
            TrackButton(
                label = "GJK",
                isActive = uiState.gjkMarker != null,
                accent = GjkRed,
                onClick = viewModel::toggleGjk
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
            // Chip koordinat marker GRB — tap: pin ke koordinat marker
            uiState.grbMarker?.let { grb ->
                CoordinateChip(
                    label = "GRB",
                    coordinate = grb,
                    accent = GrbGreen,
                    onClick = { viewModel.onMoveToRequested(grb) }
                )
            }

            // Chip koordinat marker GJK — tap: pin ke koordinat marker
            uiState.gjkMarker?.let { gjk ->
                CoordinateChip(
                    label = "GJK",
                    coordinate = gjk,
                    accent = GjkRed,
                    onClick = { viewModel.onMoveToRequested(gjk) }
                )
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

/** Cek izin fine location secara runtime. */
private fun hasFineLocationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

/** Tombol play/stop pelacakan GRB/GJK. */
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
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) accent else MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = if (isActive) Color.White else accent,
                modifier = Modifier.size(20.dp)
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
