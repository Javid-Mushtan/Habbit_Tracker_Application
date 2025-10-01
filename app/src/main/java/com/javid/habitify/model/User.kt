package com.javid.habitify.model

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false,
    val preferences: Map<String, String> = emptyMap()
) {
    fun hasEntries(): Boolean = false

    fun canEdit(): Boolean = true

    companion object {

        fun getDefaultCategories(): List<Category> = listOf(
            Category(
                id = "default_quit",
                name = "Quit a ba...",
                entryCount = 0,
                isCustom = false,
                isPremium = true
            ),
            Category(
                id = "default_art",
                name = "Art",
                entryCount = 0,
                isCustom = false,
                isPremium = false
            ),
            Category(
                id = "default_task",
                name = "Task",
                entryCount = 0,
                isCustom = false,
                isPremium = false
            ),
            Category(
                id = "default_meditation",
                name = "Meditation",
                entryCount = 0,
                isCustom = false,
                isPremium = false
            ),
            Category(
                id = "default_study",
                name = "Study",
                entryCount = 0,
                isCustom = false,
                isPremium = false
            )
        )

        fun getSampleCustomCategories(): List<Category> = listOf(
            Category(
                id = "custom_kak",
                name = "Kak",
                entryCount = 0,
                isCustom = true,
                isPremium = false
            )
        )
    }
}
