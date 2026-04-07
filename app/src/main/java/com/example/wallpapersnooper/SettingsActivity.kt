package com.example.wallpapersnooper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("wp_prefs", MODE_PRIVATE)

        // --- interval toggle ---
        val toggleIntervalUnit = findViewById<MaterialButtonToggleGroup>(R.id.toggleIntervalUnit)
        val tilIntervalValue   = findViewById<TextInputLayout>(R.id.tilIntervalValue)
        val etIntervalValue    = findViewById<TextInputEditText>(R.id.etIntervalValue)

        val savedUnit = prefs.getString("interval_unit", "unlock") ?: "unlock"
        when (savedUnit) {
            "unlock" -> toggleIntervalUnit.check(R.id.btnUnitUnlock)
            "days"   -> toggleIntervalUnit.check(R.id.btnUnitDays)
            "weeks"  -> toggleIntervalUnit.check(R.id.btnUnitWeeks)
            "months" -> toggleIntervalUnit.check(R.id.btnUnitMonths)
        }
        tilIntervalValue.visibility = if (savedUnit == "unlock") View.GONE else View.VISIBLE
        etIntervalValue.setText(prefs.getInt("interval_value", 1).toString())

        toggleIntervalUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            tilIntervalValue.visibility =
                if (checkedId == R.id.btnUnitUnlock) View.GONE else View.VISIBLE
        }

        // --- advanced section ---
        val btnAdvancedToggle = findViewById<View>(R.id.btnAdvancedToggle)
        val layoutAdvanced    = findViewById<View>(R.id.layoutAdvanced)
        val tvChevron         = findViewById<TextView>(R.id.tvChevron)

        btnAdvancedToggle.setOnClickListener {
            if (layoutAdvanced.visibility == View.GONE) {
                layoutAdvanced.visibility = View.VISIBLE
                tvChevron.text = "▲"
            } else {
                layoutAdvanced.visibility = View.GONE
                tvChevron.text = "▼"
            }
        }

        // --- service switch ---
        val switchService = findViewById<MaterialSwitch>(R.id.switchService)
        switchService.isChecked = prefs.getBoolean("service_enabled", true)

        // --- boot switch ---
        val switchBoot = findViewById<MaterialSwitch>(R.id.switchBoot2)
        switchBoot.isChecked = prefs.getBoolean("boot_enabled", true)

        // --- active tags display ---
        val tvActiveTags = findViewById<TextView>(R.id.tvActiveTags)
        val recentTags   = prefs.getStringSet("recent_tags", emptySet()) ?: emptySet()
        tvActiveTags.text = if (recentTags.isEmpty()) "(none)" else recentTags.joinToString(", ")

        // --- resolution ---
        val etResolution = findViewById<TextInputEditText>(R.id.etResolution)
        etResolution.setText(prefs.getString("resolution", "1920x1080"))

        // --- uploader ---
        val etUploader = findViewById<TextInputEditText>(R.id.etUploader)
        etUploader.setText(prefs.getString("uploader", ""))

        // --- categories ---
        val chipGroup  = findViewById<ChipGroup>(R.id.chipGroupCategories)
        val chipGeneral = findViewById<com.google.android.material.chip.Chip>(R.id.chipGeneral)
        val chipAnime   = findViewById<com.google.android.material.chip.Chip>(R.id.chipAnime)
        val chipPeople  = findViewById<com.google.android.material.chip.Chip>(R.id.chipPeople)
        chipGeneral.isChecked = prefs.getBoolean("cat_general", true)
        chipAnime.isChecked   = prefs.getBoolean("cat_anime",   false)
        chipPeople.isChecked  = prefs.getBoolean("cat_people",  false)

        // --- sorting spinner ---
        val spinnerSorting = findViewById<Spinner>(R.id.spinnerSorting)
        val sortOptions    = listOf("random", "date_added", "relevance", "views", "favorites", "toplist")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSorting.adapter = spinnerAdapter
        val savedSort = prefs.getString("sorting", "random") ?: "random"
        spinnerSorting.setSelection(sortOptions.indexOf(savedSort).coerceAtLeast(0))

        // --- save button ---
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveSettings)
        btnSave.setOnClickListener {
            val selectedUnit = when (toggleIntervalUnit.checkedButtonId) {
                R.id.btnUnitDays   -> "days"
                R.id.btnUnitWeeks  -> "weeks"
                R.id.btnUnitMonths -> "months"
                else               -> "unlock"
            }
            val intervalN = etIntervalValue.text.toString().toIntOrNull() ?: 1

            prefs.edit()
                .putString("interval_unit",    selectedUnit)
                .putInt("interval_value",       intervalN)
                .putBoolean("service_enabled",  switchService.isChecked)
                .putBoolean("boot_enabled",     switchBoot.isChecked)
                .putString("resolution",        etResolution.text.toString().trim())
                .putString("uploader",          etUploader.text.toString().trim())
                .putBoolean("cat_general",      chipGeneral.isChecked)
                .putBoolean("cat_anime",        chipAnime.isChecked)
                .putBoolean("cat_people",       chipPeople.isChecked)
                .putString("sorting",           sortOptions[spinnerSorting.selectedItemPosition])
                .apply()

            // apply service toggle immediately
            val serviceIntent = Intent(this, WallpaperService::class.java)
            if (switchService.isChecked) {
                startForegroundService(serviceIntent)
            } else {
                stopService(serviceIntent)
            }

            finish()
        }
    }
}
