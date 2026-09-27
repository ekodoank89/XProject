package com.aya.xproject.data.backup

import com.aya.xproject.data.local.FavoriteEntity
import org.json.JSONArray
import org.json.JSONObject

object FavoriteBackupCodec {
    fun encode(favorites: List<FavoriteEntity>): String {
        val jsonArray = JSONArray()
        favorites.forEach { fav ->
            val jsonObject = JSONObject().apply {
                put("id", fav.id)
                put("name", fav.name)
                put("latitude", fav.latitude)
                put("longitude", fav.longitude)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString(2)
    }

    fun decode(jsonString: String): List<FavoriteEntity> {
        val favorites = mutableListOf<FavoriteEntity>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            favorites.add(
                FavoriteEntity(
                    id = jsonObject.optInt("id", 0),
                    name = jsonObject.optString("name", ""),
                    latitude = jsonObject.optDouble("latitude", 0.0),
                    longitude = jsonObject.optDouble("longitude", 0.0)
                )
            )
        }
        return favorites
    }
}
