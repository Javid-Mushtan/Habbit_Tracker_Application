package com.javid.habitify.model

data class Category(
    val id: String = "",
    val name: String,
    val entryCount: Int = 0,
    val isCustom: Boolean = true,
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Helper methods
    fun hasEntries(): Boolean = entryCount > 0

    fun canEdit(): Boolean = isCustom || !isPremium

    companion object {
        // Default categories for easy access
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

        // Sample custom categories
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