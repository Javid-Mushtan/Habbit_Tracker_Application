package com.javid.habitify

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.javid.habitify.fragments.HomeFragment
import com.javid.habitify.fragments.SignupFragment
import com.javid.habitify.utils.PrefsManager

class MainActivity : AppCompatActivity() {
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        prefsManager = PrefsManager(this)

        if (savedInstanceState == null) {
            if (prefsManager.isLoggedIn()) {
                showHomeFragment()
            } else {
                showSignupFragment()
            }
        }
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