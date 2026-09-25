package com.xproject.hook

import android.content.Context
import android.net.Uri
import de.robv.android.xposed.XposedBridge

data class MockLocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 3.0f,
    val speed: Float = 0.0f,
    val bearing: Float = 0.0f,
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

object LocationBridge {
    private const val PROVIDER_URI = "content://com.xproject.provider.location/location"

    fun fetchMockLocation(context: Context?): MockLocationData? {
        if (context == null) return null
        return try {
            val cursor = context.contentResolver.query(
                Uri.parse(PROVIDER_URI),
                null, null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val latIdx = it.getColumnIndex("latitude")
                    val lngIdx = it.getColumnIndex("longitude")
                    val accIdx = it.getColumnIndex("accuracy")
                    val spdIdx = it.getColumnIndex("speed")
                    val brgIdx = it.getColumnIndex("bearing")
                    val activeIdx = it.getColumnIndex("is_active")
                    val tsIdx = it.getColumnIndex("timestamp")

                    val lat = if (latIdx != -1) it.getDouble(latIdx) else 0.0
                    val lng = if (lngIdx != -1) it.getDouble(lngIdx) else 0.0
                    val acc = if (accIdx != -1) it.getFloat(accIdx) else 3.0f
                    val spd = if (spdIdx != -1) it.getFloat(spdIdx) else 0.0f
                    val brg = if (brgIdx != -1) it.getFloat(brgIdx) else 0.0f
                    val active = if (activeIdx != -1) it.getInt(activeIdx) == 1 else false
                    val ts = if (tsIdx != -1) it.getLong(tsIdx) else System.currentTimeMillis()

                    MockLocationData(lat, lng, acc, spd, brg, active, ts)
                } else null
            }
        } catch (e: Throwable) {
            XposedBridge.log("XProject LocationBridge Error: ${e.message}")
            null
        }
    }
}
