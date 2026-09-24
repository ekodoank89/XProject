package com.aya.xproject.ui.jitter

import com.aya.xproject.domain.model.JitterSettings
import com.aya.xproject.domain.model.JitterTab

data class JitterUiState(
    val selectedTab: JitterTab = JitterTab.GRB,
    val settings: JitterSettings = JitterSettings.defaultFor(JitterTab.GRB)
)
