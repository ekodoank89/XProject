package com.xproject.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class LocationContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.xproject.provider.location"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/location")

        @Volatile var currentLatitude: Double = 0.0
        @Volatile var currentLongitude: Double = 0.0
        @Volatile var currentAccuracy: Float = 3.0f
        @Volatile var currentSpeed: Float = 0.0f
        @Volatile var currentBearing: Float = 0.0f
        @Volatile var isHookActive: Boolean = true

        /**
         * Panggil fungsi ini dari JitterEngine / ViewModel XProject setiap ada pembaruan koordinat
         */
        fun updateLocation(
            lat: Double,
            lng: Double,
            accuracy: Float = 3.0f,
            speed: Float = 0.0f,
            bearing: Float = 0.0f,
            active: Boolean = true
        ) {
            currentLatitude = lat
            currentLongitude = lng
            currentAccuracy = accuracy
            currentSpeed = speed
            currentBearing = bearing
            isHookActive = active
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("latitude", "longitude", "accuracy", "speed", "bearing", "is_active", "timestamp"))
        cursor.addRow(
            arrayOf(
                currentLatitude,
                currentLongitude,
                currentAccuracy,
                currentSpeed,
                currentBearing,
                if (isHookActive) 1 else 0,
                System.currentTimeMillis()
            )
        )
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.com.xproject.location"

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values?.let {
            if (it.containsKey("latitude")) currentLatitude = it.getAsDouble("latitude")
            if (it.containsKey("longitude")) currentLongitude = it.getAsDouble("longitude")
            if (it.containsKey("accuracy")) currentAccuracy = it.getAsFloat("accuracy")
            if (it.containsKey("speed")) currentSpeed = it.getAsFloat("speed")
            if (it.containsKey("bearing")) currentBearing = it.getAsFloat("bearing")
            if (it.containsKey("is_active")) isHookActive = it.getAsBoolean("is_active")
        }
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        insert(uri, values)
        return 1
    }
}
