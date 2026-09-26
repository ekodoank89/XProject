package com.aya.xproject.ui.splash

enum class SplashStep {
    INITIALIZING,
    LOADING,
    REQUEST_FOREGROUND_LOCATION,
    REQUEST_BACKGROUND_LOCATION,
    REQUEST_NOTIFICATION,
    CHECKING_BATTERY,
    DONE,
    COMPLETED
}

data class SplashUiState(
    val splashStep: SplashStep = SplashStep.INITIALIZING,
    val isLoading: Boolean = true
)
