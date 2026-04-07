package com.example.wallpapersnooper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences("wp_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("boot_enabled", true)) return

        // WorkManager is allowed from BOOT_COMPLETED, no FGS needed here
        val request = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
