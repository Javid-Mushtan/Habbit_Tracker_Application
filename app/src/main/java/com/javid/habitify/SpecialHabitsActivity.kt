package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.javid.habitify.fragments.BmiCalculatorFragment
import com.javid.habitify.fragments.FootstepFragment
import com.javid.habitify.fragments.HeartRateFragment
import com.javid.habitify.fragments.WaterTrackerFragment

class SpecialHabitsActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var mainMenuLayout: View
    private lateinit var fragmentContainer: View
    private lateinit var cardBmi: View
    private lateinit var cardWater: View
    private lateinit var cardSteps: View
    private lateinit var cardHeart: View

    private lateinit var navMain: TextView
    private lateinit var navHabits: TextView
    private lateinit var navCategories: TextView
    private lateinit var navMood: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_special_habits)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        setupToolbar()
        setupBottomNavigation()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        mainMenuLayout = findViewById(R.id.main_menu_layout)
        fragmentContainer = findViewById(R.id.fragment_container)
        cardBmi = findViewById(R.id.card_bmi)
        cardWater = findViewById(R.id.card_water)
        cardSteps = findViewById(R.id.card_steps)
        cardHeart = findViewById(R.id.card_heart)

        navMain = findViewById(R.id.navMain)
        navHabits = findViewById(R.id.navHabits)
        navCategories = findViewById(R.id.navCategories)
        navMood = findViewById(R.id.navMood)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    private fun setupBottomNavigation() {
        setBottomNavSelected(navHabits)

        navMain.setOnClickListener {
            setBottomNavSelected(navMain)
            finish()
        }

        navHabits.setOnClickListener {
            setBottomNavSelected(navHabits)
        }

        navCategories.setOnClickListener {
            setBottomNavSelected(navCategories)
            navigateToCategories()
        }

        navMood.setOnClickListener {
            setBottomNavSelected(navMood)
            navigateToMoodJournal()
        }
    }

    private fun setBottomNavSelected(selectedView: TextView) {
        val navItems = listOf(navMain, navHabits, navCategories, navMood)
        navItems.forEach { item ->
            item.setTextColor(ContextCompat.getColor(this, R.color.secondary_text))
            item.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        }

        selectedView.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        selectedView.setBackgroundColor(ContextCompat.getColor(this, R.color.nav_selected_bg))
    }

    private fun navigateToCategories() {
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMoodJournal() {
        val intent = Intent(this, MoodJournalActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupClickListeners() {
        cardBmi.setOnClickListener {
            showFragment(BmiCalculatorFragment.newInstance(), "BMI Calculator")
        }

        cardWater.setOnClickListener {
            showFragment(WaterTrackerFragment.newInstance(), "Water Drinking")
        }

        cardSteps.setOnClickListener {
            showFragment(FootstepFragment.newInstance(), "Footstep Counting")
        }

        cardHeart.setOnClickListener {
            showFragment(HeartRateFragment.newInstance(), "Heart Rate")
        }
    }

    private fun showFragment(fragment: Fragment, title: String) {
        mainMenuLayout.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        toolbar.title = title

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            mainMenuLayout.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
            toolbar.title = "Special Habits"
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun newInstance(): FootstepFragment {
            return FootstepFragment()
        }
    }
}