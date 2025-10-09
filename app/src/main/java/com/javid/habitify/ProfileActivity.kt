package com.javid.habitify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.javid.habitify.model.User
import com.javid.habitify.utils.PrefsManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var currentUser: User

    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var tvHabitsCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var progressLevel: ProgressBar
    private lateinit var tvLevel: TextView
    private lateinit var tvProgress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefsManager = PrefsManager(this)
        currentUser = prefsManager.getCurrentUser() ?: run {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        loadUserData()
    }

    private fun initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvStreakCount = findViewById(R.id.tvStreakCount)
        tvHabitsCount = findViewById(R.id.tvHabitsCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        progressLevel = findViewById(R.id.progressLevel)
        tvLevel = findViewById(R.id.tvLevel)
        tvProgress = findViewById(R.id.tvProgress)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            openEditProfile()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        ivProfilePicture.setOnClickListener {
            changeProfilePicture()
        }
    }

    private fun loadUserData() {
        tvUserName.text = currentUser.username ?: "User"
        tvUserEmail.text = currentUser.email
        tvMemberSince.text = "Member since 2024"

        val streak = prefsManager.getUserPreference("user_streak", "0")
        val habitsCount = prefsManager.getUserPreference("user_habits_count", "0")
        val completedCount = prefsManager.getUserPreference("user_completed_count", "0")

        tvStreakCount.text = streak
        tvHabitsCount.text = habitsCount
        tvCompletedCount.text = completedCount

        val totalHabits = habitsCount.toIntOrNull() ?: 0
        val completedHabits = completedCount.toIntOrNull() ?: 0
        val progress = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
        val level = (completedHabits / 10) + 1

        progressLevel.progress = progress
        tvLevel.text = "Level $level"
        tvProgress.text = "$progress%"

        loadProfilePicture()
    }

    private fun loadProfilePicture() {
        val profilePicPath = prefsManager.getUserPreference("profile_picture", "")
        if (profilePicPath.isNotEmpty()) {
            try {
                val uri = Uri.parse(profilePicPath)
                ivProfilePicture.setImageURI(uri)
            } catch (e: Exception) {
                // Handle error or set default image
                ivProfilePicture.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    private fun openEditProfile() {
        Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show()
    }

    private fun changeProfilePicture() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun logoutUser() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        prefsManager.logout()
        val intent = Intent(this, Launching::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_IMAGE_PICK -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImage: Uri? = data.data
                    selectedImage?.let { uri ->
                        ivProfilePicture.setImageURI(uri)
                        // Save the URI to user preferences using PrefsManager
                        prefsManager.setUserPreference("profile_picture", uri.toString())
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            REQUEST_EDIT_PROFILE -> {
                if (resultCode == RESULT_OK) {
                    currentUser = prefsManager.getCurrentUser() ?: return
                    loadUserData()
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
        private const val REQUEST_EDIT_PROFILE = 1002
    }
}