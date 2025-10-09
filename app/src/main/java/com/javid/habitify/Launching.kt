package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.javid.habitify.utils.PrefsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Launching : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launching)

        val sp = PrefsManager(this)

        lifecycleScope.launch {
            delay(3000)
            if (sp.isFirstLaunch()) {
                sp.setFirstLaunch(false)
                startActivity(Intent(this@Launching, Onboarding1::class.java))
            } else {
                startActivity(Intent(this@Launching, MainActivity::class.java))
            }

            finish()
        }
    }
}