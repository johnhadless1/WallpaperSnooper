package com.example.wallpapersnooper

import android.content.Context
import android.content.Intent
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import androidx.work.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WallpaperWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("wp_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("service_enabled", true)) return Result.success()

        return try {
            val tags     = prefs.getString("tags", "") ?: ""
            val res      = prefs.getString("resolution", "1920x1080") ?: "1920x1080"
            val sorting  = prefs.getString("sorting", "random") ?: "random"
            val uploader = prefs.getString("uploader", "") ?: ""
            val catG     = if (prefs.getBoolean("cat_general", true)) "1" else "0"
            val catA     = if (prefs.getBoolean("cat_anime", false)) "1" else "0"
            val catP     = if (prefs.getBoolean("cat_people", false)) "1" else "0"
            val cats     = "$catG$catA$catP"
            val q = buildString {
                if (tags.isNotEmpty()) append("&q=${tags.replace(",", "+")}")
                if (uploader.isNotEmpty()) append("&uploader=$uploader")
            }
            val url = "https://wallhaven.cc/api/v1/search?sorting=$sorting&categories=$cats&purity=100&atleast=$res$q"

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connect()
            val data = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONArray("data")
            if (data.length() == 0) return Result.success()

            val imageUrl = data.getJSONObject(0).getString("path")
            val imgConn  = URL(imageUrl).openConnection() as HttpURLConnection
            imgConn.connect()
            val bitmap = BitmapFactory.decodeStream(imgConn.inputStream)
            WallpaperManager.getInstance(applicationContext).setBitmap(bitmap)

            prefs.edit().putLong("last_changed_ms", System.currentTimeMillis()).apply()

            // start unlock listener service now that we have an active process context
            applicationContext.startForegroundService(
                Intent(applicationContext, WallpaperService::class.java)
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
