package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.javid.habitify.Onboarding1

class Onboarding3 : AppCompatActivity() {
    private lateinit var btnNext : Button
    private lateinit var btnPrevious : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding3)

        btnNext = findViewById<Button>(R.id.btnGetStarted)
        btnNext.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(Intent(this@Onboarding3, MainActivity::class.java))
            }
        })
    }
}