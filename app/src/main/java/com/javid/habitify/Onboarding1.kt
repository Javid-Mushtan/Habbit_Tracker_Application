package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.javid.habitify.fragments.LoginFragment
import com.javid.habitify.fragments.SignupFragment

class Onboarding1 : AppCompatActivity() {

    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.tvSkip)
    }

    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            val intent = Intent(this, Onboarding2::class.java)
            startActivity(intent)
            finish()
        }

        btnSkip.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            showToast("Skipping onboarding...")
        }
    }

    private fun showToast(value : String){
        Toast.makeText(this,value,Toast.LENGTH_SHORT).show()
    }
}