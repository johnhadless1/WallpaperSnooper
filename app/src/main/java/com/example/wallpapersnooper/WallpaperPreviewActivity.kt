package com.example.wallpapersnooper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import io.getstream.photoview.PhotoView
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class WallpaperPreviewActivity : AppCompatActivity() {

    private var loadedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper_preview)

        val fullUrl    = intent.getStringExtra("full_url") ?: run { finish(); return }
        val resolution = intent.getStringExtra("resolution") ?: "Unknown"
        val uploader   = intent.getStringExtra("uploader") ?: "unknown"
        val tags       = intent.getStringExtra("tags") ?: ""
        val fileSize   = intent.getLongExtra("file_size", 0L)

        val photoView   = findViewById<PhotoView>(R.id.photoView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvInfo      = findViewById<TextView>(R.id.tvInfo)
        val btnSetWallpaper = findViewById<MaterialButton>(R.id.btnSetWallpaper)
        val btnBack     = findViewById<MaterialButton>(R.id.btnBack)

        val sizeStr = if (fileSize > 0) " • ${fileSize / 1024}KB" else ""
        val tagPreview = if (tags.length > 80) tags.take(80) + "…" else tags
        tvInfo.text = "📐 $resolution  👤 $uploader$sizeStr\n🏷️ $tagPreview"

        btnBack.setOnClickListener { finish() }

        progressBar.visibility = View.VISIBLE
        btnSetWallpaper.isEnabled = false

        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val conn = URL(fullUrl).openConnection() as HttpURLConnection
                    conn.connectTimeout = 15_000
                    conn.readTimeout    = 20_000
                    conn.connect()
                    BitmapFactory.decodeStream(conn.inputStream).also { conn.disconnect() }
                }
                loadedBitmap = bitmap
                progressBar.visibility = View.GONE
                photoView.setImageBitmap(bitmap)
                btnSetWallpaper.isEnabled = true
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@WallpaperPreviewActivity,
                    "Failed to load image 😕", Toast.LENGTH_SHORT).show()
            }
        }

        btnSetWallpaper.setOnClickListener {
            val bmp = loadedBitmap ?: return@setOnClickListener
            btnSetWallpaper.isEnabled = false
            btnSetWallpaper.text = "Setting…"
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        WallpaperManager.getInstance(applicationContext).setBitmap(bmp)
                    }
                    Toast.makeText(this@WallpaperPreviewActivity,
                        "Wallpaper set! 🎉", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    btnSetWallpaper.isEnabled = true
                    btnSetWallpaper.text = "Set Wallpaper 🖼️"
                    Toast.makeText(this@WallpaperPreviewActivity,
                        "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadedBitmap = null
    }
}
