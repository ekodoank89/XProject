package com.aya.xproject.ui.favorite

import com.aya.xproject.domain.model.FavoriteTab

data class FavoriteUiState(
    val selectedTab: FavoriteTab = FavoriteTab.GRB,
    val isFormExpanded: Boolean = false,
    val form: FavoriteFormState = FavoriteFormState()
)

data class FavoriteFormState(
    val isFromPin: Boolean = true,   // true = DARI PIN, false = MANUAL
    val editingId: Long? = null,     // non-null saat mode edit
    val name: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val nameError: String? = null,
    val latitudeError: String? = null,
    val longitudeError: String? = null
) {
    val isEditing: Boolean get() = editingId != null
}

/** Bagian UI sementara (accordion + form) — tidak dipersistenkan. */
data class FavoriteFormUi(
    val isFormExpanded: Boolean = false,
    val form: FavoriteFormState = FavoriteFormState()
)
