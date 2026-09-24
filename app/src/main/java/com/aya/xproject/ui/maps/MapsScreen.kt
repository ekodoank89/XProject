package com.aya.xproject.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.domain.model.MapCenter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            // animate() butuh CameraUpdate, bukan CameraPosition
            CameraUpdateFactory.newLatLngZoom(
                LatLng(target.latitude, target.longitude),
                cameraPositionState.position.zoom
            ),
            700
        )
        viewModel.onCameraTargetConsumed()
    }

    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
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
