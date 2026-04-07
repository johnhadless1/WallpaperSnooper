package com.example.wallpapersnooper

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.*

class WallpaperService : Service() {

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_USER_PRESENT) return
            val prefs = getSharedPreferences("wp_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("service_enabled", true)) return
            if (!shouldChange(prefs)) return
            WorkManager.getInstance(applicationContext)
                .enqueue(OneTimeWorkRequestBuilder<WallpaperWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build())
        }
    }

    private fun shouldChange(prefs: SharedPreferences): Boolean {
        val unit = prefs.getString("interval_unit", "unlock") ?: "unlock"
        if (unit == "unlock") return true
        val n    = prefs.getInt("interval_value", 1)
        val last = prefs.getLong("last_changed_ms", 0L)
        val ms   = when (unit) {
            "days"   -> n * 86_400_000L
            "weeks"  -> n * 604_800_000L
            "months" -> n * 2_592_000_000L
            else     -> 0L
        }
        return System.currentTimeMillis() - last >= ms
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(
            1,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(unlockReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val channel = NotificationChannel(
            "wp_channel", "WallpaperSnooper",
            NotificationManager.IMPORTANCE_NONE
        )
        channel.setShowBadge(false)
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "wp_channel")
            .setContentTitle("WallpaperSnooper")
            .setContentText("Running in background 🖼️")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
