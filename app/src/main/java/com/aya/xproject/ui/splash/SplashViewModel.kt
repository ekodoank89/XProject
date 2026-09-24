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

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun onMinTimeElapsed() {
        _uiState.update { it.copy(isMinTimeElapsed = true) }
        advance()
    }

    fun onForegroundLocationResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                locationStatus = if (granted) PermissionItemStatus.GRANTED
                else PermissionItemStatus.DENIED,
                // Android 9 ke bawah: izin lokasi = otomatis "selalu"
                isBackgroundLocationGranted = granted && Build.VERSION.SDK_INT < 29,
                isBackgroundLocationHandled = Build.VERSION.SDK_INT < 29
            )
        }
        advance()
    }

    fun onBackgroundLocationResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isBackgroundLocationGranted = granted,
                isBackgroundLocationHandled = true
            )
        }
        advance()
    }

    fun onNotificationResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                notificationStatus = if (granted) PermissionItemStatus.GRANTED
                else PermissionItemStatus.DENIED
            )
        }
        advance()
    }

    fun onBatteryResult(allowed: Boolean) {
        _uiState.update {
            it.copy(
                batteryStatus = if (allowed) PermissionItemStatus.GRANTED
                else PermissionItemStatus.DENIED
            )
        }
        advance()
    }

    /** Escape hatch: lewati sisa permintaan izin, langsung masuk aplikasi. */
    fun skipAll() {
        _uiState.update { st ->
            st.copy(
                step = SplashStep.DONE,
                locationStatus = if (st.locationStatus == PermissionItemStatus.WAITING)
                    PermissionItemStatus.SKIPPED else st.locationStatus,
                isBackgroundLocationHandled = true,
                notificationStatus = if (st.notificationStatus == PermissionItemStatus.WAITING)
                    PermissionItemStatus.SKIPPED else st.notificationStatus,
                batteryStatus = if (st.batteryStatus == PermissionItemStatus.WAITING)
                    PermissionItemStatus.SKIPPED else st.batteryStatus
            )
        }
    }

    /** Tentukan step berikutnya berdasarkan hasil yang sudah ada. */
    private fun advance() {
        _uiState.update { st ->
            st.copy(
                step = when {
                    st.locationStatus == PermissionItemStatus.WAITING ->
                        SplashStep.REQUEST_FOREGROUND_LOCATION

                    !st.isBackgroundLocationHandled &&
                            Build.VERSION.SDK_INT >= 29 &&
                            st.locationStatus == PermissionItemStatus.GRANTED &&
                            !st.isBackgroundLocationGranted ->
                        SplashStep.REQUEST_BACKGROUND_LOCATION

                    st.notificationStatus == PermissionItemStatus.WAITING ->
                        SplashStep.REQUEST_NOTIFICATION

                    st.batteryStatus == PermissionItemStatus.WAITING ->
                        SplashStep.REQUEST_BATTERY

                    else -> SplashStep.DONE
                }
            )
        }
    }

    /** Status awal: izin yang sudah ada tidak ditanya ulang. */
    private fun initialState(): SplashUiState {
        val fineGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val backgroundGranted = Build.VERSION.SDK_INT >= 29 &&
                isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val batteryExempt = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)

        return SplashUiState(
            locationStatus = if (fineGranted) PermissionItemStatus.GRANTED
            else PermissionItemStatus.WAITING,
            isBackgroundLocationGranted = backgroundGranted || Build.VERSION.SDK_INT < 29,
            isBackgroundLocationHandled = backgroundGranted || Build.VERSION.SDK_INT < 29,
            notificationStatus = when {
                Build.VERSION.SDK_INT < 33 -> PermissionItemStatus.SKIPPED
                isGranted(Manifest.permission.POST_NOTIFICATIONS) -> PermissionItemStatus.GRANTED
                else -> PermissionItemStatus.WAITING
            },
            batteryStatus = if (batteryExempt) PermissionItemStatus.GRANTED
            else PermissionItemStatus.WAITING
        )
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
}
