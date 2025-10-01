package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding1 : AppCompatActivity() {

    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnNext = findViewById(R.id.btnNext)
    }

    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            val intent = Intent(this, Onboarding2::class.java)
            startActivity(intent)
            finish()
        }
    }
}