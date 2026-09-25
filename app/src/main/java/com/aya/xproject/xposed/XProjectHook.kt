package com.aya.xproject.xposed

import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Entry point XProject sebagai modul Xposed.
 * Nama class ini terdaftar di app/src/main/assets/xposed_init.
 *
 * MENTAL MODEL PENTING:
 * - Kode di sini TIDAK berjalan di dalam aplikasi XProject.
 *   Ia disuntikkan ke proses aplikasi lain yang masuk scope
 *   (dipilih di LSPosed Manager).
 * - Saat ini: membuktikan modul aktif via log + demo hook
 *   pada aplikasi Settings. Ganti dengan logika hook asli Anda.
 */
class XProjectHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Bukti modul dimuat ke sebuah proses — cek di LSPosed Manager > Logs
        XposedBridge.log("[XProject] Loaded into: ${lpparam.packageName}")

        // Demo hook hanya untuk scope demo (aplikasi Settings)
        if (lpparam.packageName != DEMO_SCOPE) return

        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.settings.SettingsActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        // Log saja dulu (tanpa mengubah perilaku apa pun)
                        XposedBridge.log("[XProject] Settings dibuka - modul bekerja!")
                    }
                }
            )
        }.onFailure {
            XposedBridge.log("[XProject] Demo hook gagal: ${it.message}")
        }
    }

    private companion object {
        const val DEMO_SCOPE = "com.android.settings"
    }
}
