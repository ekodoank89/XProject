package com.aya.xproject.data.backup

import com.aya.xproject.domain.model.Favorite
import com.aya.xproject.domain.model.FavoriteTab
import org.json.JSONArray
import org.json.JSONObject

/**
 * Encoder/decoder backup data favorite (format JSON).
 * Format JSON dipilih agar aman diekspor saat aplikasi berjalan,
 * mudah dibaca manusia, dan tahan perubahan skema database.
 */
object FavoriteBackupCodec {

    private const val FORMAT_VERSION = 1

    fun toJson(favorites: List<Favorite>): String {
        val array = JSONArray()
        favorites.forEach { favorite ->
            array.put(
                JSONObject()
                    .put("id", favorite.id)
                    .put("name", favorite.name)
                    .put("latitude", favorite.latitude)
                    .put("longitude", favorite.longitude)
                    .put("tab", favorite.tab.name)
            )
        }
        return JSONObject()
            .put("app", "XProject")
            .put("type", "favorites")
            .put("version", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("favorites", array)
            .toString(2)
    }

    fun fromJson(json: String): List<Favorite> {
        val root = JSONObject(json)
        require(root.optString("type") == "favorites") {
            "File bukan backup data favorite XProject"
        }
        val array = root.getJSONArray("favorites")
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Favorite(
                id = item.getLong("id"),
                name = item.getString("name"),
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude"),
                tab = FavoriteTab.valueOf(item.getString("tab"))
            )
        }
    }
}
