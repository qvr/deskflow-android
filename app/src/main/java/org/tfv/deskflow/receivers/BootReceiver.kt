package org.tfv.deskflow.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.tfv.deskflow.services.ConnectionService
import org.tfv.deskflow.services.GlobalInputService


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        if (ctx == null)
            return
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {

            ctx.startService(Intent(ctx, ConnectionService::class.java))
            ctx.startService(Intent(ctx, GlobalInputService::class.java))
        }
    }
}
