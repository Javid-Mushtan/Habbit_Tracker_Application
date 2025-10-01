package com.javid.habitify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javid.habitify.model.Category
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CategoriesViewModel : ViewModel() {

    private val _customCategories = MutableStateFlow<List<Category>>(emptyList())
    val customCategories: StateFlow<List<Category>> = _customCategories.asStateFlow()

    private val _defaultCategories = MutableStateFlow<List<Category>>(emptyList())
    val defaultCategories: StateFlow<List<Category>> = _defaultCategories.asStateFlow()

    private val _customCategoriesCount = MutableStateFlow(0)
    val customCategoriesCount: StateFlow<Int> = _customCategoriesCount.asStateFlow()

    // Change this to SharedFlow for one-time events like dialogs
    private val _showDialog = MutableSharedFlow<DialogType?>()
    val showDialog: SharedFlow<DialogType?> = _showDialog.asSharedFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _defaultCategories.value = Category.getDefaultCategories()
        _customCategories.value = Category.getSampleCustomCategories()
        _customCategoriesCount.value = _customCategories.value.size
    }

    fun showNewCategoryDialog() {
        viewModelScope.launch {
            _showDialog.emit(DialogType.NEW_CATEGORY)
        }
    }

    fun showEditCategoryDialog(category: Category) {
        viewModelScope.launch {
            _selectedCategory.value = category
            _showDialog.emit(DialogType.EDIT_CATEGORY)
        }
    }

    // ... rest of your ViewModel methods remain the same
    fun createCategory(name: String) {
        viewModelScope.launch {
            val newCategory = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                isCustom = true
            )

            val currentList = _customCategories.value.toMutableList()
            currentList.add(newCategory)

            _customCategories.value = currentList
            _customCategoriesCount.value = currentList.size
            _showDialog.emit(null)
        }
    }

    fun updateCategory(categoryId: String, newName: String) {
        viewModelScope.launch {
            val currentList = _customCategories.value.toMutableList()
            val categoryIndex = currentList.indexOfFirst { it.id == categoryId }

            if (categoryIndex != -1) {
                val updatedCategory = currentList[categoryIndex].copy(
                    name = newName,
                    updatedAt = System.currentTimeMillis()
                )
                currentList[categoryIndex] = updatedCategory
                _customCategories.value = currentList
            }

            _showDialog.emit(null)
            _selectedCategory.value = null
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val currentList = _customCategories.value.toMutableList()
            currentList.removeAll { it.id == categoryId }

            _customCategories.value = currentList
            _customCategoriesCount.value = currentList.size
            _showDialog.emit(null)
            _selectedCategory.value = null
        }
    }

    fun dismissDialog() {
        viewModelScope.launch {
            _showDialog.emit(null)
            _selectedCategory.value = null
        }
    }

    fun incrementEntryCount(categoryId: String) {
        viewModelScope.launch {
            // Update in custom categories
            val customList = _customCategories.value.toMutableList()
            val customIndex = customList.indexOfFirst { it.id == categoryId }
            if (customIndex != -1) {
                val category = customList[customIndex]
                val updatedCategory = category.copy(entryCount = category.entryCount + 1)
                customList[customIndex] = updatedCategory
                _customCategories.value = customList
                return@launch
            }

            // Update in default categories
            val defaultList = _defaultCategories.value.toMutableList()
            val defaultIndex = defaultList.indexOfFirst { it.id == categoryId }
            if (defaultIndex != -1) {
                val category = defaultList[defaultIndex]
                val updatedCategory = category.copy(entryCount = category.entryCount + 1)
                defaultList[defaultIndex] = updatedCategory
                _defaultCategories.value = defaultList
            }
        }
    }

    fun decrementEntryCount(categoryId: String) {
        viewModelScope.launch {
            // Update in custom categories
            val customList = _customCategories.value.toMutableList()
            val customIndex = customList.indexOfFirst { it.id == categoryId }
            if (customIndex != -1) {
                val category = customList[customIndex]
                val updatedCategory = category.copy(
                    entryCount = maxOf(0, category.entryCount - 1)
                )
                customList[customIndex] = updatedCategory
                _customCategories.value = customList
                return@launch
            }

            // Update in default categories
            val defaultList = _defaultCategories.value.toMutableList()
            val defaultIndex = defaultList.indexOfFirst { it.id == categoryId }
            if (defaultIndex != -1) {
                val category = defaultList[defaultIndex]
                val updatedCategory = category.copy(
                    entryCount = maxOf(0, category.entryCount - 1)
                )
                defaultList[defaultIndex] = updatedCategory
                _defaultCategories.value = defaultList
            }
        }
    }

    fun getCategoryById(categoryId: String): Category? {
        return _customCategories.value.find { it.id == categoryId }
            ?: _defaultCategories.value.find { it.id == categoryId }
    }

    enum class DialogType {
        NEW_CATEGORY, EDIT_CATEGORY
    }
}