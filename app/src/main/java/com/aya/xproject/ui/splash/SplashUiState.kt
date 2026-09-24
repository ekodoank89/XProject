package com.aya.xproject.ui.splash

/** Step aktif pada mesin state splash screen. */
enum class SplashStep {
    LOADING,
    REQUEST_FOREGROUND_LOCATION,
    REQUEST_BACKGROUND_LOCATION,
    REQUEST_NOTIFICATION,
    REQUEST_BATTERY,
    DONE
}

/** Status tiap item izin pada checklist. */
enum class PermissionItemStatus {
    WAITING,   // belum diproses
    GRANTED,   // diizinkan
    DENIED,    // ditolak pengguna
    SKIPPED    // tidak diperlukan di versi Android ini
}

data class SplashUiState(
    val isMinTimeElapsed: Boolean = false,
    val step: SplashStep = SplashStep.LOADING,
    val locationStatus: PermissionItemStatus = PermissionItemStatus.WAITING,
    val isBackgroundLocationHandled: Boolean = false,
    val isBackgroundLocationGranted: Boolean = false,
    val notificationStatus: PermissionItemStatus = PermissionItemStatus.WAITING,
    val batteryStatus: PermissionItemStatus = PermissionItemStatus.WAITING
)
