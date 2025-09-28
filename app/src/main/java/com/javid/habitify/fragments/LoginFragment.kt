package com.javid.habitify.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.javid.habitify.R

class LoginFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initViews(view)

        // Setup click listeners
        setupClickListeners()

        // Setup text watchers
        setupTextWatchers()
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnGoogle = view.findViewById(R.id.btnGoogle)
        btnFacebook = view.findViewById(R.id.btnFacebook)
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword)
        tvRegister = view.findViewById(R.id.tvRegister)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnFacebook.setOnClickListener {
            signInWithFacebook()
        }

        tvForgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }

        tvRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun setupTextWatchers() {
        // Clear errors when user starts typing
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearEmailError()
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearPasswordError()
            }
        })
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(email, password)) {
            performLogin(email, password)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        // Clear previous errors
        clearEmailError()
        clearPasswordError()

        // Validate email
        when {
            email.isEmpty() -> {
                setEmailError("Email is required")
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                setEmailError("Please enter a valid email address")
                isValid = false
            }
        }

        // Validate password
        when {
            password.isEmpty() -> {
                setPasswordError("Password is required")
                isValid = false
            }
            password.length < 6 -> {
                setPasswordError("Password must be at least 6 characters")
                isValid = false
            }
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        // Show loading state
        setLoginButtonState(isLoading = true)

        // Simulate API call delay
        Handler(Looper.getMainLooper()).postDelayed({
            // This is where you'd make your actual API call
            if (isValidCredentials(email, password)) {
                onLoginSuccess()
            } else {
                onLoginFailure()
            }
            setLoginButtonState(isLoading = false)
        }, 2000)
    }

    private fun isValidCredentials(email: String, password: String): Boolean {
        // For demo purposes - replace with actual authentication logic
        return email.isNotEmpty() && password.length >= 6
    }

    private fun onLoginSuccess() {
        showToast("Login successful!")
        navigateToMainScreen()
    }

    private fun onLoginFailure() {
        showToast("Invalid email or password")
    }

    private fun setLoginButtonState(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Logging in..." else "Login"
    }

    private fun signInWithGoogle() {
        // Implement Google Sign-In logic here
        showToast("Google Sign-In clicked")
    }

    private fun signInWithFacebook() {
        // Implement Facebook Sign-In logic here
        showToast("Facebook Sign-In clicked")
    }

    private fun navigateToForgotPassword() {
        showToast("Navigate to Forgot Password")
        // Using Navigation Component
        // findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    private fun navigateToRegister() {
        showToast("Navigate to Register")
        // Using Navigation Component
        // findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun navigateToMainScreen() {
        showToast("Navigating to main screen...")
        // Navigate to your main activity or home fragment
        // Example using Navigation Component:
        // findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    // Helper methods for error handling
    private fun setEmailError(error: String) {
        etEmail.error = error
    }

    private fun setPasswordError(error: String) {
        etPassword.error = error
    }

    private fun clearEmailError() {
        etEmail.error = null
    }

    private fun clearPasswordError() {
        etPassword.error = null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}