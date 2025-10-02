package com.javid.habitify.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.javid.habitify.model.User
import java.util.UUID
import androidx.core.content.edit

class PrefsManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "HabitifyPrefs"

        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_USERS = "users"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LAST_EMAIL = "last_email"
        public const val KEY_FIRST_LAUNCH = "first_launch"
    }

    public val sharedPref: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPref.edit { putBoolean(KEY_IS_LOGGED_IN, isLoggedIn) }
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPref.edit { putString(KEY_CURRENT_USER, userJson) }
    }

    fun getCurrentUser(): User? {
        val userJson = sharedPref.getString(KEY_CURRENT_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun logout() {
        sharedPref.edit {
            remove(KEY_CURRENT_USER)
            putBoolean(KEY_IS_LOGGED_IN, false)
            putBoolean(KEY_REMEMBER_ME, false)
        }
    }

    fun setRememberMe(remember: Boolean) {
        sharedPref.edit { putBoolean(KEY_REMEMBER_ME, remember) }
    }

    fun shouldRememberMe(): Boolean {
        return sharedPref.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun setLastEmail(email: String) {
        sharedPref.edit { putString(KEY_LAST_EMAIL, email) }
    }

    fun getLastEmail(): String {
        return sharedPref.getString(KEY_LAST_EMAIL, "") ?: ""
    }

    fun registerUser(user: User): Boolean {
        val users = getAllUsers().toMutableList()

        if (users.any { it.email.equals(user.email, ignoreCase = true) }) {
            return false
        }

        users.add(user)
        saveAllUsers(users)
        return true
    }

    fun loginUser(email: String, password: String): User? {
        val users = getAllUsers()
        val user = users.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }

        user?.let {
            val loggedInUser = it.copy(isLoggedIn = true)
            setCurrentUser(loggedInUser)
            setLoggedIn(true)
            return loggedInUser
        }

        return null
    }

    fun getUserByEmail(email: String): User? {
        val users = getAllUsers()
        return users.find { it.email.equals(email, ignoreCase = true) }
    }

    fun updateUser(updatedUser: User): Boolean {
        val users = getAllUsers().toMutableList()
        val userIndex = users.indexOfFirst { it.id == updatedUser.id }

        if (userIndex != -1) {
            users[userIndex] = updatedUser
            saveAllUsers(users)

            val currentUser = getCurrentUser()
            if (currentUser?.id == updatedUser.id) {
                setCurrentUser(updatedUser)
            }
            return true
        }
        return false
    }

    fun deleteUser(userId: String): Boolean {
        val users = getAllUsers().toMutableList()
        val userIndex = users.indexOfFirst { it.id == userId }

        if (userIndex != -1) {
            users.removeAt(userIndex)
            saveAllUsers(users)

            val currentUser = getCurrentUser()
            if (currentUser?.id == userId) {
                logout()
            }
            return true
        }
        return false
    }

    private fun getAllUsers(): List<User> {
        val usersJson = sharedPref.getString(KEY_USERS, "[]")
        return try {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson<List<User>>(usersJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAllUsers(users: List<User>) {
        val usersJson = gson.toJson(users)
        sharedPref.edit { putString(KEY_USERS, usersJson) }
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        sharedPref.edit { putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch) }
    }

    fun isFirstLaunch(): Boolean {
        return sharedPref.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun createDemoUsers() {
        if (getAllUsers().isEmpty()) {
            val demoUsers = listOf(
                User(
                    id = UUID.randomUUID().toString(),
                    email = "demo@habitify.com",
                    username = "Demo User",
                    password = "123456",
                    isLoggedIn = false
                ),
                User(
                    id = UUID.randomUUID().toString(),
                    email = "test@habitify.com",
                    username = "Test User",
                    password = "123456",
                    isLoggedIn = false
                )
            )
            saveAllUsers(demoUsers)
        }
    }

    fun setUserPreference(key: String, value: String) {
        val user = getCurrentUser()
        user?.let {
            val preferences = it.preferences.toMutableMap()
            preferences[key] = value
            val updatedUser = it.copy(preferences = preferences)
            updateUser(updatedUser)
        }
    }

    fun getUserPreference(key: String, defaultValue: String = ""): String {
        val user = getCurrentUser()
        return user?.preferences?.get(key) ?: defaultValue
    }

    fun clearAllData() {
        sharedPref.edit { clear() }
    }

    fun hasUsers(): Boolean {
        return getAllUsers().isNotEmpty()
    }

    fun getUserCount(): Int {
        return getAllUsers().size
    }
}