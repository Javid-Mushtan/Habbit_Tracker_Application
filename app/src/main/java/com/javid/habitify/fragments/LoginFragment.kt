package com.javid.habitify.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.javid.habitify.MainActivity
import com.javid.habitify.R
import com.javid.habitify.utils.PrefsManager

class LoginFragment : Fragment() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView

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
        initViews(view)
        setupClickListeners()
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvSignup = view.findViewById(R.id.tvSignup)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        tvSignup.setOnClickListener {
            navigateToSignup()
        }
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

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        val user = prefsManager.loginUser(email, password)

        if (user != null) {
            onLoginSuccess(user.username)
        } else {
            onLoginFailure()
        }
    }

    private fun onLoginSuccess(username: String) {
        showToast("Welcome back, $username!")
        (requireActivity() as? MainActivity)?.showHomeFragmentAfterLogin()
    }

    private fun onLoginFailure() {
        showToast("Invalid email or password")
    }

    private fun navigateToSignup() {
        val signupFragment = SignupFragment.newInstance()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, signupFragment)
            .addToBackStack("login")
            .commit()
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