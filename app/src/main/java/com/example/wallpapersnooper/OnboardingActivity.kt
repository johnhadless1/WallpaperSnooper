package com.example.wallpapersnooper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

data class OnboardPage(
    val emoji: String,
    val title: String,
    val desc: String,
    val showToggles: Boolean = false
)

class OnboardingActivity : AppCompatActivity() {

    private val pages = listOf(
        OnboardPage("🖼️", "Welcome to WallpaperSnooper",
            "Automatically change your wallpaper using millions of images from Wallhaven. Search by tag, style, category — and let the app do the rest."),
        OnboardPage("🔍", "Search by Tags",
            "Type any tag like \"abstract\", \"anime\" or \"cyberpunk\" and browse results instantly. Tap any image to preview it full screen."),
        OnboardPage("⚙️", "Auto-Change Wallpaper",
            "WallpaperSnooper can change your wallpaper every unlock, every few days, weeks or months — completely automatically in the background."),
        OnboardPage("🔒", "Permissions & Settings",
            "Choose what you want enabled. You can always change these later in Settings.", showToggles = true)
    )

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if already done, skip straight to MainActivity
        val prefs = getSharedPreferences("wp_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_done", false)) {
            prefs.edit().putBoolean("onboarding_done", true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabIndicator)
        val btnNext   = findViewById<MaterialButton>(R.id.btnNext)

        viewPager.adapter = OnboardAdapter(this, pages)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                btnNext.text = if (position == pages.lastIndex) "Let's Go! 🚀" else "Next"
            }
        })

        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < pages.lastIndex) {
                viewPager.currentItem = current + 1
            } else {
                val frag = supportFragmentManager
                    .findFragmentByTag("f${pages.lastIndex}") as? OnboardPageFragment
                frag?.let {
                    prefs.edit()
                        .putBoolean("boot_enabled",    it.switchBoot?.isChecked ?: true)
                        .putBoolean("service_enabled", it.switchBackground?.isChecked ?: true)
                        .apply()

                    if (it.switchNotif?.isChecked == true &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                        notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // use commit() so it's definitely saved before we navigate
                prefs.edit().putBoolean("onboarding_done", true).commit()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

class OnboardAdapter(fa: FragmentActivity, private val pages: List<OnboardPage>)
    : FragmentStateAdapter(fa) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int) = OnboardPageFragment.newInstance(pages[position])
}

class OnboardPageFragment : Fragment() {

    var switchBoot: MaterialSwitch? = null
    var switchBackground: MaterialSwitch? = null
    var switchNotif: MaterialSwitch? = null

    companion object {
        fun newInstance(page: OnboardPage) = OnboardPageFragment().apply {
            arguments = Bundle().apply {
                putString("emoji",        page.emoji)
                putString("title",        page.title)
                putString("desc",         page.desc)
                putBoolean("showToggles", page.showToggles)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_onboarding_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.tvEmoji).text = arguments?.getString("emoji")
        view.findViewById<TextView>(R.id.tvTitle).text = arguments?.getString("title")
        view.findViewById<TextView>(R.id.tvDesc).text  = arguments?.getString("desc")

        val layoutToggles = view.findViewById<View>(R.id.layoutToggles)
        switchBoot        = view.findViewById(R.id.switchBoot)
        switchBackground  = view.findViewById(R.id.switchBackground)
        switchNotif       = view.findViewById(R.id.switchNotif)

        if (arguments?.getBoolean("showToggles") == true) {
            layoutToggles.visibility = View.VISIBLE
        }
    }
}