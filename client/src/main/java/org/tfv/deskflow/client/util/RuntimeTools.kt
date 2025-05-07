package org.tfv.deskflow.client.util

fun isAndroidRuntime(): Boolean {
    return try {
        // “android.os.Build” only exists on Android
        Class.forName("android.os.Build")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}