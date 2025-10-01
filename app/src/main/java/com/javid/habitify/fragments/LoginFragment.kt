package com.javid.habitify.fragments

import android.content.Context
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
import com.javid.habitify.utils.PrefsManager

class LoginFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView
    private lateinit var prefsManager: PrefsManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefsManager = PrefsManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAutoLogin()
        initViews(view)
        setupClickListeners()
        setupTextWatchers()
        loadRememberedEmail()
    }

    private fun checkAutoLogin() {
        if (prefsManager.isLoggedIn()) {
            navigateToHomeFragment()
        }
    }

    private fun loadRememberedEmail() {
        if (prefsManager.shouldRememberMe()) {
            val lastEmail = prefsManager.getLastEmail()
            if (lastEmail.isNotEmpty()) {
                etEmail.setText(lastEmail)
            }
        }
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

        clearEmailError()
        clearPasswordError()

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
        setLoginButtonState(isLoading = true)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = prefsManager.loginUser(email, password)

            if (user != null) {
                prefsManager.setRememberMe(true)
                prefsManager.setLastEmail(email)
                onLoginSuccess(user.username)
            } else {
                onLoginFailure()
            }
            setLoginButtonState(isLoading = false)
        }, 1500)
    }

    // ❌ REMOVE THIS OLD METHOD - We're using actual PrefsManager login now
    // private fun isValidCredentials(email: String, password: String): Boolean {
    //     // Simple demo validation - replace with your actual logic
    //     return email.isNotEmpty() && password.length >= 6
    // }

    private fun onLoginSuccess(username: String) {
        showToast("Welcome back, $username!")
        navigateToHomeFragment()
    }

    private fun onLoginFailure() {
        showToast("Invalid email or password")

        // Show specific error messages
        val email = etEmail.text.toString().trim()
        if (prefsManager.getUserByEmail(email) == null) {
            // User doesn't exist
            setEmailError("No account found with this email")
        } else {
            // User exists but wrong password
            setPasswordError("Incorrect password")
        }
    }

    // ✅ NAVIGATE TO HOME FRAGMENT
    private fun navigateToHomeFragment() {
        val homeFragment = HomeFragment.newInstance()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()
    }

    private fun setLoginButtonState(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Logging in..." else "Login"
    }

    private fun signInWithGoogle() {
        showToast("Google Sign-In clicked")
        // Simulate login for demo
        simulateSocialLogin()
    }

    private fun signInWithFacebook() {
        showToast("Facebook Sign-In clicked")
        // Simulate login for demo
        simulateSocialLogin()
    }

    private fun simulateSocialLogin() {
        setLoginButtonState(isLoading = true)
        Handler(Looper.getMainLooper()).postDelayed({
            // For social login, create a demo user or use existing
            val demoUser = prefsManager.getCurrentUser()
            if (demoUser == null) {
                // If no user exists, show message
                showToast("Please sign up first or use demo account")
            } else {
                showToast("Social login successful!")
                navigateToHomeFragment()
            }
            setLoginButtonState(isLoading = false)
        }, 1500)
    }

    private fun navigateToForgotPassword() {
        showToast("Navigate to Forgot Password")
        // Implement ForgotPasswordFragment navigation if needed
    }

    private fun navigateToRegister() {
        showToast("Navigate to Register")
        // Navigate to SignupFragment
        val signupFragment = SignupFragment.newInstance()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, signupFragment)
            .addToBackStack("register")
            .commit()
    }

    // Helper methods for error handling
    private fun setEmailError(error: String) {
        etEmail.error = error
        etEmail.requestFocus()
    }

    private fun setPasswordError(error: String) {
        etPassword.error = error
        etPassword.requestFocus()
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