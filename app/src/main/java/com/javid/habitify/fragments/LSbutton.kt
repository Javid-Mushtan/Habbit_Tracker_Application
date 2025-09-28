package com.javid.habitify.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.javid.habitify.R

class LSbutton : Fragment() {

    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var btnApple: Button
    private lateinit var btnPassword: Button
    private lateinit var txtRegister: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_l_sbutton, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        btnGoogle = view.findViewById(R.id.btnGoogle)
        btnFacebook = view.findViewById(R.id.btnFacebook)
        btnApple = view.findViewById(R.id.btnApple)
        btnPassword = view.findViewById(R.id.btnPassword)
        txtRegister = view.findViewById(R.id.txtRegister)
    }

    private fun setupClickListeners() {
        btnGoogle.setOnClickListener {
            showToast("Google Sign In Clicked")
        }

        btnFacebook.setOnClickListener {
            showToast("Facebook Sign In Clicked")
        }

        btnApple.setOnClickListener {
            showToast("Apple Sign In Clicked")
        }

        btnPassword.setOnClickListener {
            showToast("Password Sign In Clicked")
        }

        txtRegister.setOnClickListener {
            showToast("Register Now Clicked")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}