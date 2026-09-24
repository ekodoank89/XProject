package com.aya.xproject.ui.splash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(readInitial())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun onMinTimeElapsed() {
        _uiState.update { it.copy(isMinTimeElapsed = true) }
        advance()
    }

    fun onForegroundLocationResult(granted: Boolean) {
        _uiState.update { it.copy(locationStatus = statusOf(granted)) }
        advance()
    }

    fun onBackgroundLocationResult(granted: Boolean) {
        _uiState.update { it.copy(backgroundLocationStatus = statusOf(granted)) }
        advance()
    }

    fun onNotificationResult(granted: Boolean) {
        _uiState.update { it.copy(notificationStatus = statusOf(granted)) }
        advance()
    }

    fun onBatteryResult(allowed: Boolean) {
        _uiState.update { it.copy(batteryStatus = statusOf(allowed)) }
        advance()
    }

    /**
     * Baca ulang status izin asli dari sistem.
     * Dipanggil saat kembali dari halaman Pengaturan: izin yang baru
     * diberikan akan berubah jadi GRANTED, sisanya tetap (tombol tetap tampil).
     */
    fun refreshPermissions() {
        _uiState.update { st ->
            st.copy(
                locationStatus = if (isGranted(Manifest.permission.ACCESS_FINE_LOCATION))
                    PermissionItemStatus.GRANTED else st.locationStatus,
                backgroundLocationStatus = when {
                    !requiresBackground() -> PermissionItemStatus.SKIPPED
                    isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ->
                        PermissionItemStatus.GRANTED
                    else -> st.backgroundLocationStatus
                },
                notificationStatus = when {
                    !requiresNotification() -> PermissionItemStatus.SKIPPED
                    isGranted(Manifest.permission.POST_NOTIFICATIONS) ->
                        PermissionItemStatus.GRANTED
                    else -> st.notificationStatus
                },
                batteryStatus = if (isBatteryExempt()) PermissionItemStatus.GRANTED
                else st.batteryStatus
            )
        }
        advance()
    }

    /**
     * Tentukan step berikutnya. ATURAN GERBANG:
     * step DONE hanya tercapai jika SEMUA izin wajib berstatus GRANTED.
     * Izin yang ditolak membuat step berhenti di izin tersebut.
     */
    private fun advance() {
        _uiState.update { st ->
            st.copy(
                step = when {
                    st.locationStatus != PermissionItemStatus.GRANTED ->
                        SplashStep.REQUEST_FOREGROUND_LOCATION

                    requiresBackground() &&
                            st.backgroundLocationStatus != PermissionItemStatus.GRANTED ->
                        SplashStep.REQUEST_BACKGROUND_LOCATION

                    requiresNotification() &&
                            st.notificationStatus != PermissionItemStatus.GRANTED ->
                        SplashStep.REQUEST_NOTIFICATION

                    st.batteryStatus != PermissionItemStatus.GRANTED ->
                        SplashStep.REQUEST_BATTERY

                    else -> SplashStep.DONE
                }
            )
        }
    }

    private fun statusOf(granted: Boolean) =
        if (granted) PermissionItemStatus.GRANTED else PermissionItemStatus.DENIED

    private fun requiresBackground() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun requiresNotification() = Build.VERSION.SDK_INT >= 33

    private fun isBatteryExempt() =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

    /** Status awal: izin yang sudah ada dianggap GRANTED (tidak ditanya ulang). */
    private fun readInitial() = SplashUiState(
        locationStatus = if (isGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            PermissionItemStatus.GRANTED else PermissionItemStatus.WAITING,
        backgroundLocationStatus = when {
            !requiresBackground() -> PermissionItemStatus.SKIPPED
            isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ->
                PermissionItemStatus.GRANTED
            else -> PermissionItemStatus.WAITING
        },
        notificationStatus = when {
            !requiresNotification() -> PermissionItemStatus.SKIPPED
            isGranted(Manifest.permission.POST_NOTIFICATIONS) ->
                PermissionItemStatus.GRANTED
            else -> PermissionItemStatus.WAITING
        },
        batteryStatus = if (isBatteryExempt()) PermissionItemStatus.GRANTED
        else PermissionItemStatus.WAITING
    )
}
