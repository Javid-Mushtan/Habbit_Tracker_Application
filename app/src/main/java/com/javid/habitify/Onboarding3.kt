package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.javid.habitify.Onboarding1
import com.javid.habitify.Onboarding2

class Onboarding3 : AppCompatActivity() {
    private lateinit var btnNext : Button
    private lateinit var btnPrevious : Button
    private lateinit var btnSkip : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding3)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnNext = findViewById(R.id.btnGetStarted)
        btnSkip = findViewById(R.id.tvSkip3)
        btnPrevious = findViewById(R.id.btnPrevious3)
    }

    private fun setupClickListeners() {
        btnSkip.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            showToast("Skipping onboarding...")
        }

        btnNext.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(Intent(this@Onboarding3, MainActivity::class.java))
            }
        })

        btnPrevious.setOnClickListener{
            val intent = Intent(this, Onboarding2::class.java)
            startActivity(intent)
            finish()

            showToast("jumping to previous...")
        }
    }

    private fun showToast(value : String){
        Toast.makeText(this,value,Toast.LENGTH_SHORT).show()
    }
}