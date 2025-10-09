package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.javid.habitify.fragments.CategoriesFragment
import com.javid.habitify.fragments.HomeFragment
import com.javid.habitify.fragments.MoodJournalFragment
import com.javid.habitify.fragments.SignupFragment
import com.javid.habitify.fragments.SpecialHabitsFragment
import com.javid.habitify.utils.PrefsManager

class MainActivity : AppCompatActivity() {
    private lateinit var prefsManager: PrefsManager

    private lateinit var navMain: TextView
    private lateinit var navHabits: TextView
    private lateinit var navMood: TextView
    private lateinit var navCategories: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        prefsManager = PrefsManager(this)

        navMain = findViewById(R.id.navMain)
        navHabits = findViewById(R.id.navHabits)
        navMood = findViewById(R.id.navMood)
        navCategories = findViewById(R.id.navCategories)

        if (savedInstanceState == null) {
            if (prefsManager.isLoggedIn()) {
                showHomeFragment()
            } else {
                showSignupFragment()
            }
        }

        setupBottomNavigation()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupBottomNavigation() {
        setBottomNavSelected(navMain)

        navCategories.setOnClickListener {
            setBottomNavSelected(navCategories)
            loadFragment(CategoriesFragment())
        }

        navMain.setOnClickListener {
            setBottomNavSelected(navMain)
            loadFragment(HomeFragment())
        }

        navHabits.setOnClickListener {
            setBottomNavSelected(navHabits)
            loadFragment(SpecialHabitsFragment())
        }

        navMood.setOnClickListener {
            setBottomNavSelected(navMood)
            loadFragment(MoodJournalFragment())
        }
    }

    private fun setBottomNavSelected(selectedView: TextView) {
        val navItems = listOf(navMain, navHabits, navMood)
        navItems.forEach { item ->
            item.setTextColor(ContextCompat.getColor(this, R.color.secondary_text))
            item.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        }

        selectedView.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        selectedView.setBackgroundColor(ContextCompat.getColor(this, R.color.nav_selected_bg))
    }

    private fun showHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance())
            .commit()
    }

    private fun showSignupFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SignupFragment.newInstance())
            .commit()
    }

    fun showHomeFragmentAfterLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance())
            .commit()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}