package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.javid.habitify.utils.PrefsManager

class Launching : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launching)

        var sp : PrefsManager = PrefsManager(this)

        println(sp.sharedPref)
        if(sp.isFirstLaunch()){
            sp.setFirstLaunch(true)
            startActivity(Intent(this@Launching, Onboarding1::class.java))
        } else {
            startActivity(Intent(this@Launching, MainActivity::class.java))
        }
    }
}