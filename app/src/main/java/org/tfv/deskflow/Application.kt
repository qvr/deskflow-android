package org.tfv.deskflow

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import com.google.android.material.color.DynamicColors
import org.tfv.deskflow.client.util.logging.KLoggingManager

class Application : Application() {

    companion object {
        private val log = KLoggingManager.logger(Application::class)
    }

    override fun onCreate() {
        super.onCreate()

        log.info { "onCreate:${this::class.java.simpleName}" }
        DynamicColors.applyToActivitiesIfAvailable(this)

        setStrictModePolicy()

    }

    /**
     * Return true if the application is debuggable.
     */
    private fun isDebuggable(): Boolean {
        return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }

    /**
     * Set a thread policy that detects all potential problems on the main thread, such as network
     * and disk access.
     *
     * If a problem is found, the offending call will be logged and the application will be killed.
     */
    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                Builder().detectAll().penaltyLog().build(),
            )
        }
    }

}