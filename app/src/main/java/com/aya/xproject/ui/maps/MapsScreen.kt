package com.aya.xproject.ui.maps

import android.Manifest
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

/** Zoom maksimal yang didukung Google Maps. */
private const val MAX_ZOOM = 21f

/** Zoom default aplikasi — target tombol zoom out. */
private const val DEFAULT_ZOOM = 15f

/** Durasi animasi kamera untuk tombol kontrol (ms). */
private const val CAMERA_ANIMATION_MS = 500

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ===== Titik biru lokasi (my location) =====
    // Flag hanya boleh true jika izin fine location benar-benar terpenuhi,
    // kalau tidak app akan crash (SecurityException).
    var isMyLocationEnabled by remember {
        mutableStateOf(hasFineLocationPermission(context))
    }

    // Saat kembali ke app (mis. dari Settings setelah mencabut/memberi izin),
    // status izin dicek ulang agar titik biru mengikuti kondisi terkini.
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

    // Ada permintaan pindah kamera (dari menu Favorite) → animasikan, lalu tandai selesai
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

    /** Auto focus: kamera fokus kembali ke titik tengah (pin) + fungsi kompas:
     *  arah kamera direset menghadap utara (bearing 0) dan tilt diratakan. */
    fun autoFocus() {
        scope.launch {
            val current = cameraPositionState.position
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    // 4 parameter: target, zoom, tilt, bearing
                    CameraPosition(current.target, current.zoom, 0f, 0f)
                ),
                CAMERA_ANIMATION_MS
            )
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

    /** Zoom out: sekali tap langsung kembali ke zoom default (bukan bertahap). */
    fun zoomOutToDefault() {
        scope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.zoomTo(DEFAULT_ZOOM),
                CAMERA_ANIMATION_MS
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                // Menampilkan titik biru lokasi pengguna di peta
                isMyLocationEnabled = isMyLocationEnabled
            ),
            uiSettings = MapUiSettings(
                compassEnabled = false,      // kompas bawaan Google dimatikan
                zoomControlsEnabled = false, // tombol +/- bawaan dimatikan
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        )

        // Pin overlay: selalu di tengah layar.
        // Offset -24dp (setengah tinggi ikon 48dp) agar ujung pin tepat di titik tengah.
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .offset(y = (-24).dp)
        )

        // ===== Tombol kontrol peta buatan sendiri =====
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
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
                contentDescription = "Zoom ke default",
                onClick = { zoomOutToDefault() }
            )
        }

        // Chip koordinat: hanya tampil jika switch di menu OPT aktif
        if (uiState.isCoordinateChipVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
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

/** Cek izin fine location secara runtime. */
private fun hasFineLocationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

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
