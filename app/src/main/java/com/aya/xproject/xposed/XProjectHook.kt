package com.aya.xproject.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Entry point XProject sebagai modul Xposed.
 *
 * VERSI HARDENED (verifikasi tahap 1):
 * - TIDAK meng-hook method apa pun. Hanya menulis satu baris log
 *   saat modul dimuat ke sebuah proses.
 * - LSPosed hanya menyuntikkan modul ini ke aplikasi dalam scope
 *   (saat ini: Settings), proses lain tidak tersentuh.
 *
 * Setelah terbukti boot stabil + log muncul, hook target asli
 * ditambahkan secara bertahap di iterasi berikutnya.
 */
class XProjectHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        runCatching {
            XposedBridge.log("[XProject] Loaded into: ${lpparam.packageName}")
        }
    }
}