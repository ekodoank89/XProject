package com.aya.xproject.data.repository

import com.aya.xproject.domain.model.Favorite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor() {

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    fun add(favorite: Favorite) = _favorites.update { it + favorite }

    fun update(favorite: Favorite) = _favorites.update { list ->
        list.map { if (it.id == favorite.id) favorite else it }
    }

    fun delete(id: Long) = _favorites.update { list ->
        list.filterNot { it.id == id }
    }

    fun nextId(): Long = System.currentTimeMillis()
}
