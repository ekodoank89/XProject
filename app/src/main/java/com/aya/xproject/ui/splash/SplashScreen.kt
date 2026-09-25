package com.aya.xproject.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xproject.R

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val splashStep by viewModel.splashStep.collectAsStateWithLifecycle()

    LaunchedEffect(splashStep) {
        if (splashStep == SplashStep.COMPLETED) {
            onNavigateNext()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Splash Logo",
            modifier = Modifier.size(120.dp)
        )
        CircularProgressIndicator()
    }
}

enum class SplashStep {
    INITIALIZING,
    CHECKING_PERMISSIONS,
    COMPLETED
}
