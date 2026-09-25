package com.xproject.hook

import android.content.Context
import android.location.Location
import android.os.SystemClock
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {

    companion object {
        const val TAG = "XProject_LSPosed"
        val TARGET_PACKAGES = setOf(
            "com.gojek.partner",
            "com.grabtaxi.driver2"
        )
        @Volatile var appContext: Context? = null
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName !in TARGET_PACKAGES) return

        XposedBridge.log("$TAG: Hooking target application -> ${lpparam.packageName}")

        // Tangkap Context Aplikasi saat terpasang
        XposedHelpers.findAndHookMethod(
            "android.app.Application",
            lpparam.classLoader,
            "attach",
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    appContext = param.args[0] as? Context
                    XposedBridge.log("$TAG: App Context captured for ${lpparam.packageName}")
                }
            }
        )

        // Hook Getter objek Location
        hookLocationClass()

        // Hook Android LocationManager System Service
        hookLocationManager(lpparam.classLoader)

        // Hook Google Play Services Fused Location Client
        hookFusedLocationProvider(lpparam.classLoader)
    }

    private fun hookLocationClass() {
        // Hook Latitude
        XposedHelpers.findAndHookMethod(
            Location::class.java,
            "getLatitude",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mockData = LocationBridge.fetchMockLocation(appContext)
                    if (mockData != null && mockData.isActive && mockData.latitude != 0.0) {
                        param.result = mockData.latitude
                    }
                }
            }
        )

        // Hook Longitude
        XposedHelpers.findAndHookMethod(
            Location::class.java,
            "getLongitude",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mockData = LocationBridge.fetchMockLocation(appContext)
                    if (mockData != null && mockData.isActive && mockData.longitude != 0.0) {
                        param.result = mockData.longitude
                    }
                }
            }
        )

        // Hook Accuracy
        XposedHelpers.findAndHookMethod(
            Location::class.java,
            "getAccuracy",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mockData = LocationBridge.fetchMockLocation(appContext)
                    if (mockData != null && mockData.isActive && mockData.latitude != 0.0) {
                        param.result = mockData.accuracy
                    }
                }
            }
        )
    }

    private fun hookLocationManager(classLoader: ClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.LocationManager",
                classLoader,
                "getLastKnownLocation",
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val mockData = LocationBridge.fetchMockLocation(appContext)
                        if (mockData != null && mockData.isActive && mockData.latitude != 0.0) {
                            val originalLoc = param.result as? Location ?: Location(param.args[0] as? String ?: "gps")
                            originalLoc.latitude = mockData.latitude
                            originalLoc.longitude = mockData.longitude
                            originalLoc.accuracy = mockData.accuracy
                            originalLoc.speed = mockData.speed
                            originalLoc.bearing = mockData.bearing
                            originalLoc.time = System.currentTimeMillis()
                            originalLoc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                            param.result = originalLoc
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            XposedBridge.log("$TAG: Error hooking LocationManager: ${e.message}")
        }
    }

    private fun hookFusedLocationProvider(classLoader: ClassLoader) {
        try {
            val locationResultClass = XposedHelpers.findClassIfExists(
                "com.google.android.gms.location.LocationResult",
                classLoader
            )
            if (locationResultClass != null) {
                XposedHelpers.findAndHookMethod(
                    locationResultClass,
                    "getLastLocation",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val mockData = LocationBridge.fetchMockLocation(appContext)
                            if (mockData != null && mockData.isActive && mockData.latitude != 0.0) {
                                val loc = param.result as? Location ?: Location("fused")
                                loc.latitude = mockData.latitude
                                loc.longitude = mockData.longitude
                                loc.accuracy = mockData.accuracy
                                loc.speed = mockData.speed
                                loc.bearing = mockData.bearing
                                loc.time = System.currentTimeMillis()
                                loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                                param.result = loc
                            }
                        }
                    }
                )

                XposedHelpers.findAndHookMethod(
                    locationResultClass,
                    "getLocations",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val mockData = LocationBridge.fetchMockLocation(appContext)
                            if (mockData != null && mockData.isActive && mockData.latitude != 0.0) {
                                val locations = param.result as? List<*>
                                locations?.forEach { obj ->
                                    if (obj is Location) {
                                        obj.latitude = mockData.latitude
                                        obj.longitude = mockData.longitude
                                        obj.accuracy = mockData.accuracy
                                        obj.speed = mockData.speed
                                        obj.bearing = mockData.bearing
                                        obj.time = System.currentTimeMillis()
                                        obj.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        } catch (e: Throwable) {
            XposedBridge.log("$TAG: Error hooking FusedLocationProvider: ${e.message}")
        }
    }
}
