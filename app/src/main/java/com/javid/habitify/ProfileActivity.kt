package com.javid.habitify

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var btnChangePassword: Button
    private lateinit var btnNotificationSettings: Button
    private lateinit var btnPrivacySettings: Button
    private lateinit var btnHelpSupport: Button
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
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnNotificationSettings = findViewById(R.id.btnNotificationSettings)
        btnPrivacySettings = findViewById(R.id.btnPrivacySettings)
        btnHelpSupport = findViewById(R.id.btnHelpSupport)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            //openEditProfile()
        }

        btnChangePassword.setOnClickListener {
            //openChangePassword()
        }

        btnNotificationSettings.setOnClickListener {
            //openNotificationSettings()
        }

        btnPrivacySettings.setOnClickListener {
            //openPrivacySettings()
        }

        btnHelpSupport.setOnClickListener {
            //openHelpSupport()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        // Profile picture click to change
        ivProfilePicture.setOnClickListener {
            changeProfilePicture()
        }
    }

    private fun loadUserData() {
        // Load user basic info
        tvUserName.text = currentUser.username ?: "User"
        tvUserEmail.text = currentUser.email
        tvMemberSince.text = "Member since 2024" // You can store join date in User model

        // Load user stats (you can store these in SharedPreferences or User model)
        val streak = prefsManager.getUserPreference("user_streak", "0")
        val habitsCount = prefsManager.getUserPreference("user_habits_count", "0")
        val completedCount = prefsManager.getUserPreference("user_completed_count", "0")

        tvStreakCount.text = streak
        tvHabitsCount.text = habitsCount
        tvCompletedCount.text = completedCount

        // Calculate level and progress (example logic)
        val totalHabits = habitsCount.toIntOrNull() ?: 0
        val completedHabits = completedCount.toIntOrNull() ?: 0
        val progress = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
        val level = (completedHabits / 10) + 1 // Level up every 10 completed habits

        progressLevel.progress = progress
        tvLevel.text = "Level $level"
        tvProgress.text = "$progress%"

        // Load profile picture if exists
        val profilePicPath = prefsManager.getUserPreference("profile_picture", "")
        if (profilePicPath.isNotEmpty()) {
            // Load image from storage (you'll need to implement this)
            // Glide.with(this).load(File(profilePicPath)).into(ivProfilePicture)
        }
    }

//    private fun openEditProfile() {
//        val intent = Intent(this, EditProfileActivity::class.java)
//        startActivityForResult(intent, REQUEST_EDIT_PROFILE)
//    }
//
//    private fun openChangePassword() {
//        val intent = Intent(this, ChangePasswordActivity::class.java)
//        startActivity(intent)
//    }
//
//    private fun openNotificationSettings() {
//        val intent = Intent(this, NotificationSettingsActivity::class.java)
//        startActivity(intent)
//    }
//
//    private fun openPrivacySettings() {
//        val intent = Intent(this, PrivacySettingsActivity::class.java)
//        startActivity(intent)
//    }
//
//    private fun openHelpSupport() {
//        val intent = Intent(this, HelpSupportActivity::class.java)
//        startActivity(intent)
//    }

    private fun changeProfilePicture() {
        // Implement image picker logic here
        Toast.makeText(this, "Change profile picture feature", Toast.LENGTH_SHORT).show()
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
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Refresh user data if profile was edited
            currentUser = prefsManager.getCurrentUser() ?: return
            loadUserData()
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_EDIT_PROFILE = 1001
    }
}