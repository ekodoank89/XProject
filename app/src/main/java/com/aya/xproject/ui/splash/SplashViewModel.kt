package com.aya.xproject.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        startSplashFlow()
    }

    private fun startSplashFlow() {
        viewModelScope.launch {
            _uiState.update { it.copy(splashStep = SplashStep.REQUEST_FOREGROUND_LOCATION) }
        }
    }

    fun onForegroundLocationHandled() {
        _uiState.update { it.copy(splashStep = SplashStep.REQUEST_BACKGROUND_LOCATION) }
    }

    fun onBackgroundLocationHandled() {
        _uiState.update { it.copy(splashStep = SplashStep.REQUEST_NOTIFICATION) }
    }

    fun onNotificationHandled() {
        _uiState.update { it.copy(splashStep = SplashStep.CHECKING_BATTERY) }
    }

    fun onBatteryOptimizationHandled() {
        _uiState.update { it.copy(splashStep = SplashStep.DONE) }
    }

    fun onSplashCompleted() {
        _uiState.update { it.copy(splashStep = SplashStep.COMPLETED) }
    }
}
