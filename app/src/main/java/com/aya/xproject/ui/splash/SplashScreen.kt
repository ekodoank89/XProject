package com.aya.xproject.ui.splash

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aya.xproject.R
import kotlinx.coroutines.delay

private const val SPLASH_MIN_DURATION_MS = 2000L

// Warna splash ditetapkan tetap agar konsisten di tema terang/gelap
private val SplashBackground = Color(0xFF1565C0)
private val SplashOnBackground = Color.White

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ===== Launcher hasil izin =====
    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onForegroundLocationResult(granted) }

    // Dialog "Allow all the time" hanya tersedia di Android 10 (API 29)
    val backgroundDialogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onBackgroundLocationResult(granted) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onNotificationResult(granted) }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onBatteryResult(result.resultCode == Activity.RESULT_OK)
    }

    // Launcher halaman Pengaturan: saat kembali, status izin dicek ulang
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshPermissions() }

    fun openAppSettings() {
        settingsLauncher.launch(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        )
    }

    fun requestBatteryExemption() {
        runCatching {
            batteryLauncher.launch(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${context.packageName}"))
            )
        }.onFailure { viewModel.onBatteryResult(false) }
    }

    // ===== Mesin state splash =====
    // 1. Splash tampil minimal beberapa saat
    LaunchedEffect(Unit) {
        delay(SPLASH_MIN_DURATION_MS)
        viewModel.onMinTimeElapsed()
    }

    // 2. Jalankan dialog sistem sesuai step aktif
    LaunchedEffect(uiState.step) {
        when (uiState.step) {
            SplashStep.REQUEST_FOREGROUND_LOCATION ->
                foregroundLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

            SplashStep.REQUEST_BACKGROUND_LOCATION ->
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    backgroundDialogLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    openAppSettings()
                }

            SplashStep.REQUEST_NOTIFICATION ->
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            SplashStep.REQUEST_BATTERY -> requestBatteryExemption()

            SplashStep.LOADING, SplashStep.DONE -> Unit
        }
    }

    // 3. Selesai (semua izin diberikan) → masuk aplikasi otomatis
    LaunchedEffect(uiState.step) {
        if (uiState.step == SplashStep.DONE) {
            delay(600)
            onFinished()
        }
    }

    // ===== Tombol pemulihan untuk izin yang ditolak =====
    val deniedAction: Pair<String, () -> Unit>? = when (uiState.step) {
        SplashStep.REQUEST_FOREGROUND_LOCATION ->
            if (uiState.locationStatus == PermissionItemStatus.DENIED)
                "Buka Pengaturan — izinkan Lokasi" to { openAppSettings() } else null

        SplashStep.REQUEST_BACKGROUND_LOCATION ->
            if (uiState.backgroundLocationStatus == PermissionItemStatus.DENIED)
                "Buka Pengaturan — pilih Lokasi: Allow all the time" to { openAppSettings() }
            else null

        SplashStep.REQUEST_NOTIFICATION ->
            if (uiState.notificationStatus == PermissionItemStatus.DENIED)
                "Buka Pengaturan — izinkan Notifikasi" to { openAppSettings() } else null

        SplashStep.REQUEST_BATTERY ->
            if (uiState.batteryStatus == PermissionItemStatus.DENIED)
                "Coba Lagi — Izinkan Baterai" to { requestBatteryExemption() } else null

        else -> null
    }

    // Label khusus item lokasi: detail "selalu" vs "saat digunakan"
    val requiresBackground = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val locationLabel: String? = when {
        uiState.locationStatus == PermissionItemStatus.GRANTED &&
                (!requiresBackground ||
                        uiState.backgroundLocationStatus == PermissionItemStatus.GRANTED) ->
            "Selalu diizinkan"

        uiState.locationStatus == PermissionItemStatus.GRANTED -> "Saat digunakan"
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ===== Logo dari gambar Anda (app/src/main/res/drawable/splash_logo) =====
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(96.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo XProject",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.size(16.dp))

            Text(
                text = "XProject",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SplashOnBackground
            )
            Text(
                text = "Simpan & tuju lokasi favorit Anda",
                style = MaterialTheme.typography.bodyMedium,
                color = SplashOnBackground.copy(alpha = 0.85f)
            )

            Spacer(Modifier.size(24.dp))

            if (!uiState.isMinTimeElapsed) {
                CircularProgressIndicator(color = SplashOnBackground)
            }

            // ===== Kartu checklist izin =====
            AnimatedVisibility(visible = uiState.isMinTimeElapsed) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.widthIn(max = 340.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Izin yang diperlukan",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Semua izin wajib diberikan sebelum masuk aplikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(12.dp))

                        PermissionRow(
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = "Lokasi",
                            status = uiState.locationStatus,
                            statusLabelOverride = locationLabel
                        )
                        Spacer(Modifier.size(8.dp))

                        if (requiresBackground) {
                            PermissionRow(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                title = "Lokasi latar belakang (selalu)",
                                status = uiState.backgroundLocationStatus
                            )
                            Spacer(Modifier.size(8.dp))
                        }

                        if (Build.VERSION.SDK_INT >= 33) {
                            PermissionRow(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                title = "Notifikasi",
                                status = uiState.notificationStatus
                            )
                            Spacer(Modifier.size(8.dp))
                        }

                        PermissionRow(
                            icon = {
                                // Emoji agar tidak perlu library ikon extended (hemat ukuran APK)
                                Text(text = "🔋", fontSize = 16.sp)
                            },
                            title = "Baterai tanpa pembatasan",
                            status = uiState.batteryStatus
                        )

                        Spacer(Modifier.size(12.dp))

                        if (uiState.step == SplashStep.DONE) {
                            Button(
                                onClick = onFinished,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Mulai") }
                        } else {
                            deniedAction?.let { (label, action) ->
                                Button(
                                    onClick = action,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(label) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: @Composable () -> Unit,
    title: String,
    status: PermissionItemStatus,
    statusLabelOverride: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        when (status) {
            PermissionItemStatus.GRANTED -> {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = statusLabelOverride ?: "Diberikan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PermissionItemStatus.DENIED -> {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = statusLabelOverride ?: "Ditolak",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            PermissionItemStatus.WAITING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = statusLabelOverride ?: "Menunggu",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PermissionItemStatus.SKIPPED -> {
                Text(
                    text = statusLabelOverride ?: "Tidak diperlukan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
