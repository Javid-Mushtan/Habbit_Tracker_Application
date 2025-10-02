package com.javid.habitify.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.javid.habitify.MainActivity
import com.javid.habitify.R
import com.javid.habitify.model.User
import com.javid.habitify.utils.PrefsManager
import java.util.UUID

class SignupFragment : Fragment() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ibTogglePassword: ImageButton
    private lateinit var ibToggleConfirmPassword: ImageButton
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignUp: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var already: TextView

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefsManager = PrefsManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()
        setupTextWatchers()
    }

    private fun initViews(view: View) {
        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        ibTogglePassword = view.findViewById(R.id.ibTogglePassword)
        ibToggleConfirmPassword = view.findViewById(R.id.ibToggleConfirmPassword)
        cbTerms = view.findViewById(R.id.cbTerms)
        btnSignUp = view.findViewById(R.id.btnSignUp)
        btnGoogle = view.findViewById(R.id.btnGoogle)
        btnFacebook = view.findViewById(R.id.btnFacebook)
        tvLogin = view.findViewById(R.id.tvLogin)
        progressBar = view.findViewById(R.id.progressBar)
        already = view.findViewById(R.id.already)
    }

    private fun setupClickListeners() {
        // Use only ONE click listener to avoid conflicts
        val loginClickListener = View.OnClickListener {
            navigateToLogin()
        }

        already.setOnClickListener(loginClickListener)
        tvLogin.setOnClickListener(loginClickListener)

        btnSignUp.setOnClickListener {
            attemptSignUp()
        }

        btnGoogle.setOnClickListener {
            signUpWithGoogle()
        }

        btnFacebook.setOnClickListener {
            signUpWithFacebook()
        }

        ibTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        ibToggleConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearErrors()
            }
        }

        etFullName.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)
    }

    private fun attemptSignUp() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (validateInputs(fullName, email, password, confirmPassword)) {
            performSignUp(fullName, email, password)
        }
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        clearErrors()

        if (fullName.isEmpty()) {
            setFullNameError("Full name is required")
            isValid = false
        } else if (fullName.length < 2) {
            setFullNameError("Full name must be at least 2 characters")
            isValid = false
        }

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

        when {
            confirmPassword.isEmpty() -> {
                setConfirmPasswordError("Please confirm your password")
                isValid = false
            }
            password != confirmPassword -> {
                setConfirmPasswordError("Passwords do not match")
                isValid = false
            }
        }

        if (!cbTerms.isChecked) {
            showToast("Please accept the Terms of Service and Privacy Policy")
            isValid = false
        }

        return isValid
    }

    private fun performSignUp(fullName: String, email: String, password: String) {
        setSignUpButtonState(isLoading = true)

        Handler(Looper.getMainLooper()).postDelayed({
            val newUser = User(
                id = UUID.randomUUID().toString(),
                email = email,
                username = fullName,
                password = password,
                isLoggedIn = true
            )

            val isRegistered = prefsManager.registerUser(newUser)

            if (isRegistered) {
                prefsManager.setCurrentUser(newUser)
                prefsManager.setLoggedIn(true)

                onSignUpSuccess(fullName)
            } else {
                onSignUpFailure()
            }
            setSignUpButtonState(isLoading = false)
        }, 2000)
    }

    private fun onSignUpSuccess(fullName: String) {
        showToast("Welcome $fullName! Account created successfully!")
        (requireActivity() as? MainActivity)?.showHomeFragmentAfterLogin()
    }

    private fun onSignUpFailure() {
        showToast("Email already exists. Please use a different email.")
    }

    private fun navigateToLogin() {
        try {
            val loginFragment = LoginFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .addToBackStack("signup")
                .commit()

            showToast("Navigating to login...")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error: Could not open login screen")
        }
    }

    private fun signUpWithGoogle() {
        showToast("Google Sign-Up clicked")
    }

    private fun signUpWithFacebook() {
        showToast("Facebook Sign-Up clicked")
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            etPassword.transformationMethod = null
            // ibTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            // ibTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        etPassword.setSelection(etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            etConfirmPassword.transformationMethod = null
            // ibToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            // ibToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        }
        etConfirmPassword.setSelection(etConfirmPassword.text.length)
    }

    private fun setSignUpButtonState(isLoading: Boolean) {
        btnSignUp.isEnabled = !isLoading
        btnSignUp.text = if (isLoading) "Creating Account..." else "Sign Up"
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setFullNameError(error: String) {
        etFullName.error = error
    }

    private fun setEmailError(error: String) {
        etEmail.error = error
    }

    private fun setPasswordError(error: String) {
        etPassword.error = error
    }

    private fun setConfirmPasswordError(error: String) {
        etConfirmPassword.error = error
    }

    private fun clearErrors() {
        etFullName.error = null
        etEmail.error = null
        etPassword.error = null
        etConfirmPassword.error = null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(): SignupFragment {
            return SignupFragment()
        }
    }
}