package com.example.wallpapersnooper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var chipGroup: ChipGroup
    private lateinit var hsvChips: HorizontalScrollView
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // skip to onboarding if not done yet
        prefs = getSharedPreferences("wp_prefs", MODE_PRIVATE)

        if (!prefs.getBoolean("onboarding_done", false)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        requestSamsungBatteryExemption()

        prefs = getSharedPreferences("wp_prefs", MODE_PRIVATE)

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        if (prefs.getBoolean("service_enabled", true)) {
            startForegroundService(Intent(this, WallpaperService::class.java))
        }

        val etTags = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.etTags)
        val suggestions = listOf(
            "abstract", "aesthetic", "ai art", "aircraft", "airplane", "alien", "alternative", "ambient", "ancient", "angel", "animals", "anime", "apocalypse", "aqua", "arcade", "architecture", "art", "artistic", "artwork", "astral", "astronomy", "atmospheric", "autumn",
            "badass", "balance", "baroque", "battle", "beach", "beautiful", "bicycle", "biomechanical", "birds", "black", "black and white", "blade", "blue", "boat", "bokeh", "bold", "bridge", "brutalist", "building", "buildings", "burning",
            "calm", "car", "cars", "cartoon", "castle", "casual", "cat", "celestial", "character", "chaos", "chill", "city", "cityscape", "clean", "clouds", "coastal", "color", "colorful", "comfy", "comic", "concept art", "contemporary", "contrast", "cosmic", "cozy", "cyber", "cyberpunk",
            "dark", "dark fantasy", "dawn", "daylight", "deep space", "desert", "design", "detailed", "digital", "digital art", "distopian", "dream", "dreamy", "drone", "dusk", "earth", "eerie", "elegant", "emotional", "epic", "ethereal", "evening",
            "fantasy", "fashion", "female", "fire", "fish", "flat design", "floral", "flowers", "flow", "fog", "forest", "futuristic",
            "galaxy", "game", "gaming", "geometric", "girl", "glitch", "glow", "gold", "gothic", "gradient", "graffiti", "green", "grunge",
            "hacker", "hair", "hand drawn", "haze", "heaven", "hell", "hero", "high tech", "hills", "horizon", "horror", "house", "human", "hyperrealistic", "ice", "illustration", "industrial", "infinite", "ink", "interior", "island",
            "japan", "japanese", "jungle", "kawaii", "knight", "lake", "landscape", "light", "lightning", "line art", "lonely",
            "macro", "magic", "magical", "male", "man", "mechanical", "medieval", "melancholy", "minimal", "minimalism", "mist", "modern", "monochrome", "monster", "moon", "mountain", "mountains", "mystery",
            "nature", "nebula", "neon", "night", "night sky", "noir", "nostalgia", "ocean", "old", "orange", "outdoors",
            "painting", "paradise", "pastel", "pattern", "peaceful", "people", "photography", "pink", "pixel", "pixel art", "planet", "portrait", "post apocalyptic", "purple",
            "rain", "rainy", "realistic", "red", "reflection", "relaxing", "retro", "river", "road", "robot", "romantic",
            "sad", "samurai", "scenery", "sci-fi", "sea", "serene", "shadow", "ship", "sky", "skyscraper", "sleepy", "snow", "soft", "space", "spaceship", "sparkle", "spring", "stars", "storm", "street", "summer", "sun", "sunlight", "sunrise", "sunset", "surreal",
            "technology", "temple", "texture", "thunder", "timeless", "tower", "town", "trees", "tropical", "urban", "vaporwave", "vehicle", "village", "vintage", "void",
            "wallpaper", "warm", "warrior", "water", "waterfall", "waves", "white", "wild", "wind", "window", "winter", "woman", "woods", "world", "zen",
            "abandoned", "abyss", "acid", "acrobat", "aerial", "afterlife", "aggressive", "airship", "alley", "amazing", "ancient ruins", "androgynous", "anime girl", "anime boy", "anthro", "apocalyptic city", "arcane", "armor", "armored", "art deco", "artifact", "assassin", "astronaut", "asymmetric",
            "backlit", "battlefield", "beast", "berserk", "bioluminescent", "blade runner style", "blizzard", "blood", "bloom", "blur", "boardwalk", "bold colors", "botanical", "broken", "bronze", "brutal",
            "cabin", "candlelight", "canyon", "cel shading", "ceramic", "chaotic", "checkerboard", "child", "chinese", "chrome", "cinematic", "circuit", "cliff", "clothing", "collapsed", "color splash", "combat", "comet", "cosplay", "crystal", "cyborg",
            "dancer", "danger", "dead trees", "decay", "decorative", "deep sea", "demon", "dense", "destroyed", "devil", "dim", "dinosaur", "distortion", "divine", "double exposure", "dramatic", "drift", "dust",
            "earth tones", "electric", "elf", "embroidery", "energy", "engine", "enormous", "environment", "eruption", "escape", "evolution", "explosion", "expressionism",
            "fabric", "fairy", "falling", "fantasy armor", "feathers", "field", "fighter", "fireworks", "floating", "fluid", "foggy", "foliage", "fortress", "fractal", "frozen", "futurism",
            "galactic", "garden", "gas mask", "giant", "glass", "glitchcore", "gloomy", "goddess", "golden hour", "graffiti art", "gravity", "grim", "guardian",
            "halloween", "harbor", "hard surface", "haunted", "heavy rain", "heroic", "high contrast", "high detail", "hologram", "holy", "horizon line", "hover", "hunter",
            "iceberg", "icy", "idol", "imaginary", "immersive", "imperial", "ink wash", "insane", "intense", "intricate", "invasion", "isometric", "jewel", "journey", "judge", "kaiju", "katana", "king", "kingdom",
            "labyrinth", "laser", "lava", "leaf", "legend", "levitating", "light rays", "liquid", "lone figure", "lost", "low poly", "luminous",
            "machine", "mage", "magenta", "majestic", "marble", "mask", "mech", "mecha", "melting", "metal", "meteor", "micro", "military", "mirror", "misty", "moody", "motion", "multiverse", "muted",
            "narrative", "natural light", "navy", "neural", "night city", "ninja", "nordic", "obsidian", "occult", "oil painting", "old town", "organic", "ornate", "overcast",
            "palace", "panorama", "particle", "path", "phantom", "phoenix", "pier", "pillar", "plasma", "poetic", "portal", "postcard", "power", "prism", "psychedelic", "punk", "queen", "quiet",
            "radiant", "rainforest", "rebellion", "reflection water", "render", "retro future", "ruins", "rust",
            "sacred", "sakura", "sand", "scifi city", "sculpture", "shadowy", "shattered", "shipwreck", "shrine", "silhouette", "silver", "skeletal", "smoke", "smooth", "solitude", "spark", "spectral", "speed", "sphere", "spiritual", "splatter", "spooky", "steampunk", "stone", "structure", "subtle", "survivor", "swamp",
            "tactical", "tattoo", "techwear", "temple ruins", "tentacle", "terrain", "themed", "throne", "titan", "torch", "totem", "tranquil", "translucent", "trap", "tribal", "underground", "underwater", "unreal", "urban decay", "utopia", "valley", "vast", "vibrant", "victorian", "view", "virtual", "vision", "volcano",
            "wanderer", "war", "wasteland", "watchtower", "weapon", "wet", "whimsical", "wilderness", "windy", "witch", "wizard", "wreck", "x-ray", "youth", "zenith",
            "abstract expressionism", "acid trip", "aerial view", "afterglow", "alien landscape", "ancient temple", "angel wings", "anime aesthetic", "anime wallpaper", "arc reactor style", "arctic", "art nouveau", "artstation style", "astral plane", "at dusk", "aurora",
            "background blur", "badlands", "barbed wire", "battle armor", "battle scene", "beach sunset", "beyond horizon", "biopunk", "black metal", "blazing", "blocky", "blue hour", "body paint", "borderlands style", "botanical garden", "burnt",
            "cafe", "california vibe", "candid", "car interior", "car meet", "celestial body", "chalk", "character design", "city lights", "city night", "city rain", "cliffside", "cloudscape", "cold tones", "color grading", "comic style", "concrete", "cosmic horror", "cracked", "creature", "crimson", "cross", "crowd",
            "cyber city", "cyber samurai", "cyberpunk city", "cyberpunk street", "cyberpunk neon",
            "dark mode", "dark sky", "deep forest", "deep ocean", "deep shadows", "desaturated", "destruction", "devastation", "digital painting", "distant", "doodle", "dreamcore", "dripping", "dust storm",
            "early morning", "earthquake", "eclipse", "edge lighting", "electric blue", "empty", "endless", "energy field", "epic landscape", "eroded", "ethnic", "explorer",
            "face", "fallen", "fantasy creature", "fantasy landscape", "far future", "fashion model", "feudal japan", "film grain", "firestorm", "first person", "flat colors", "floating island", "foggy forest", "folk", "food", "framed", "frost",
            "game art", "game wallpaper", "gas station", "gears", "ghost", "giant robot", "glacial", "glassy", "gloom", "golden", "goth", "grainy", "graphic design", "greenery",
            "hand", "harsh light", "hd", "headphones", "heavily detailed", "high resolution", "highway", "holographic", "home", "hometown", "hoodie", "horror aesthetic", "hot", "hyper detailed",
            "ice cave", "illustrated", "immersed", "in the rain", "indie", "infinite space", "ink drawing", "inner city", "inside", "introspection", "isolated", "japanese street", "jungle ruins", "knife", "korean",
            "landscape art", "late night", "layered", "leaves", "light beam", "light glow", "light trails", "liminal", "liminal space", "loneliness", "long exposure", "lost city",
            "machine world", "macro photography", "magic circle", "magical girl", "manhwa style", "marvel style", "matrix style", "meadow", "melancholic", "midnight", "minimal art", "misty mountains", "modern city", "moody lighting", "motion blur", "mountain peak", "mystic",
            "nature wallpaper", "neon city", "neon lights", "night lights", "night rain", "no people", "noisy", "ocean view", "old building", "old street", "open world", "orange sky", "outfit",
            "painterly", "palm trees", "paper", "parallel world", "park", "particle effects", "pastel colors", "peace", "person", "photorealistic", "pink sky", "pixelated", "planet surface", "portrait art", "post apocalypse", "powerful", "pretty", "procedural", "quiet night",
            "rain drops", "rainy city", "real life", "reflection lake", "relaxed", "rendered", "retro city", "retro neon", "rim light", "river bank", "road trip", "ruined city",
            "sadness", "scary", "scifi landscape", "screen wallpaper", "sea waves", "shadow figure", "sharp", "shiny", "shore", "side view", "simple", "sketch", "skyline", "slow", "small town", "soft light", "space art", "space travel", "sparkles", "spirit", "split tone", "star field", "starry sky", "street art", "street lights", "street night", "stylized", "sun rays", "sunset beach", "surreal art",
            "tech", "tech aesthetic", "temple japan", "texture detail", "third person", "thick fog", "thin lines", "tidal", "timelapse", "tiny", "tokyo", "top view", "town night", "tranquility", "travel", "tree line", "tunnel", "twilight", "undead", "urban night", "urban street", "utopian",
            "vapor", "vaporwave aesthetic", "vast landscape", "vertical", "vfx", "vignette", "village night", "void space",
            "wall art", "warm light", "water surface", "water reflection", "wave", "wavy", "wide angle", "wild nature", "window light", "winter forest", "wireframe", "world building", "yellow", "young", "zen garden"
        )
        val adapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, suggestions
        )
        etTags.setAdapter(adapter)
        val btnSearch    = findViewById<MaterialButton>(R.id.btnSearch)
        val btnSettings  = findViewById<MaterialButton>(R.id.btnSettings)
        val rvWallpapers = findViewById<RecyclerView>(R.id.rvWallpapers)
        val tvStatus     = findViewById<TextView>(R.id.tvStatus)
        chipGroup        = findViewById(R.id.chipGroup)
        hsvChips         = findViewById(R.id.hsvChips)

        rvWallpapers.layoutManager = GridLayoutManager(this, 2)

        // Restore saved tag chips
        val savedTags = prefs.getString("tags", "") ?: ""
        if (savedTags.isNotEmpty()) {
            savedTags.split(",").forEach { t -> val s = t.trim(); if (s.isNotEmpty()) addTagChip(s) }
            hsvChips.visibility = View.VISIBLE
            fetchWallpapers(tvStatus, rvWallpapers)
        }

        fun doSearch() {
            val tag = etTags.text.toString().trim()
            if (tag.isEmpty()) { fetchWallpapers(tvStatus, rvWallpapers); return }
            addTagChip(tag)
            etTags.setText("")
            hsvChips.visibility = View.VISIBLE
            getSystemService(InputMethodManager::class.java)
                .hideSoftInputFromWindow(etTags.windowToken, 0)
            fetchWallpapers(tvStatus, rvWallpapers)
        }

        btnSearch.setOnClickListener { doSearch() }
        etTags.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun requestSamsungBatteryExemption() {
        val prefs = getSharedPreferences("wp_prefs", MODE_PRIVATE)
        // only show once, only on Samsung
        if (prefs.getBoolean("samsung_prompt_shown", false)) return
        if (!android.os.Build.MANUFACTURER.equals("samsung", ignoreCase = true)) return

        prefs.edit().putBoolean("samsung_prompt_shown", true).apply()

        android.app.AlertDialog.Builder(this)
            .setTitle("⚡ Samsung Detected")
            .setMessage(
                "Samsung's battery optimizer can stop WallpaperSnooper from " +
                        "running in the background and changing your wallpaper.\n\n" +
                        "Tap 'Fix It' to open battery settings — set WallpaperSnooper to Unrestricted."
            )
            .setPositiveButton("Fix It") { _, _ ->
                try {
                    // opens directly to this app's battery settings page
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    // fallback to general battery optimization settings
                    startActivity(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }
            }
            .setNegativeButton("Not Now", null)
            .show()
    }


    private fun addTagChip(tag: String) {
        // avoid duplicates
        for (i in 0 until chipGroup.childCount) {
            if ((chipGroup.getChildAt(i) as Chip).text == tag) return
        }
        val chip = Chip(this)
        chip.text = tag
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
            if (chipGroup.childCount == 0) hsvChips.visibility = View.GONE
            saveTags()
        }
        chipGroup.addView(chip)
        saveTags()
    }

    private fun saveTags() {
        val tags = (0 until chipGroup.childCount)
            .map { (chipGroup.getChildAt(it) as Chip).text.toString() }
            .joinToString(",")
        prefs.edit().putString("tags", tags).apply()
    }

    private fun fetchWallpapers(tvStatus: TextView, rv: RecyclerView) {
        if (chipGroup.childCount == 0) { tvStatus.text = "Add a tag and press search 🔍"; return }

        val tags = (0 until chipGroup.childCount)
            .map { (chipGroup.getChildAt(it) as Chip).text.toString() }
            .joinToString("+")

        val res      = prefs.getString("resolution", "1920x1080") ?: "1920x1080"
        val sorting  = prefs.getString("sorting", "random") ?: "random"
        val catG     = if (prefs.getBoolean("cat_general", true)) "1" else "0"
        val catA     = if (prefs.getBoolean("cat_anime", false)) "1" else "0"
        val catP     = if (prefs.getBoolean("cat_people", false)) "1" else "0"
        val cats     = "$catG$catA$catP"
        val uploader = prefs.getString("uploader", "") ?: ""
        val uploaderQ = if (uploader.isNotEmpty()) "&uploader=$uploader" else ""

        val url = "https://wallhaven.cc/api/v1/search?sorting=$sorting&categories=$cats&purity=100&atleast=$res&q=$tags$uploaderQ"

        tvStatus.text = "Loading... 🔄"
        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connect()
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                val data = json.getJSONArray("data")
                val items = (0 until data.length()).map {
                    val obj  = data.getJSONObject(it)
                    val w    = obj.optInt("dimension_x", 0)
                    val h    = obj.optInt("dimension_y", 0)
                    val user = obj.optJSONObject("uploader")?.optString("username") ?: "unknown"
                    val tagsArr = obj.optJSONArray("tags")
                    val tagStr = if (tagsArr != null)
                        (0 until tagsArr.length()).joinToString(", ") {
                                i -> tagsArr.getJSONObject(i).optString("name", "")
                        } else ""
                    val fSize = obj.optLong("file_size", 0L)
                    WallpaperItem(
                        thumbUrl   = obj.getJSONObject("thumbs").getString("large"),
                        fullUrl    = obj.getString("path"),
                        resolution = if (w > 0) "${w}×${h}" else "Unknown",
                        uploader   = user,
                        tags       = tagStr,
                        fileSize   = fSize
                    )
                }
                runOnUiThread {
                    rv.adapter = WallpaperAdapter(items) { selected ->
                        startActivity(
                            Intent(this, WallpaperPreviewActivity::class.java)
                                .putExtra("full_url",   selected.fullUrl)
                                .putExtra("resolution", selected.resolution)
                                .putExtra("uploader",   selected.uploader)
                                .putExtra("tags",       selected.tags)
                                .putExtra("file_size",  selected.fileSize)
                        )
                    }
                    tvStatus.text = "${items.size} wallpapers found — tap to preview 🎨"
                }
            } catch (e: Exception) {
                runOnUiThread { tvStatus.text = "Error fetching wallpapers 😢" }
            }
        }.start()
    }
}
