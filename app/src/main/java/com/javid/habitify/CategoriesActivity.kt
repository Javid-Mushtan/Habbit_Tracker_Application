package com.javid.habitify

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.javid.habitify.adapters.CategoryAdapter
import com.javid.habitify.model.Category
import com.javid.habitify.viewmodel.CategoriesViewModel
import kotlinx.coroutines.flow.collectLatest

class CategoriesActivity : AppCompatActivity() {

    private val viewModel: CategoriesViewModel by viewModels()

    private lateinit var rvCustomCategories: RecyclerView
    private lateinit var rvDefaultCategories: RecyclerView
    private lateinit var tvCustomCount: TextView
    private lateinit var tvPremiumInfo: TextView
    private lateinit var btnNewCategory: Button
    private lateinit var btnBack: ImageButton
    private lateinit var emptyState: LinearLayout
    private lateinit var customCategoriesCard: LinearLayout
    private lateinit var defaultCategoriesCard: LinearLayout

    private lateinit var customCategoryAdapter: CategoryAdapter
    private lateinit var defaultCategoryAdapter: CategoryAdapter

    private var newCategoryDialog: AlertDialog? = null
    private var editCategoryDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        initializeViews()
        setupAdapters()
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews() {
        rvCustomCategories = findViewById(R.id.rvCustomCategories)
        rvDefaultCategories = findViewById(R.id.rvDefaultCategories)
        tvCustomCount = findViewById(R.id.tvCustomCount)
        tvPremiumInfo = findViewById(R.id.tvPremiumInfo)
        btnNewCategory = findViewById(R.id.btnNewCategory)
        btnBack = findViewById(R.id.btnBack)
        emptyState = findViewById(R.id.emptyState)
        customCategoriesCard = findViewById(R.id.customCategoriesCard)
        defaultCategoriesCard = findViewById(R.id.defaultCategoriesCard)
    }

    private fun setupAdapters() {
        customCategoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                onCategoryClicked(category)
            },
            onEditClick = { category ->
                viewModel.showEditCategoryDialog(category)
            },
            showEditButton = true
        )

        defaultCategoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                onCategoryClicked(category)
            },
            onEditClick = { category ->
                if (category.isPremium) {
                    showPremiumRequiredDialog()
                } else {
                    viewModel.showEditCategoryDialog(category)
                }
            },
            showEditButton = false
        )
    }

    private fun setupRecyclerViews() {
        rvCustomCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
            adapter = customCategoryAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@CategoriesActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        rvDefaultCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
            adapter = defaultCategoryAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@CategoriesActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.customCategories.collectLatest { categories ->
                customCategoryAdapter.submitList(categories)
                updateEmptyState()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.defaultCategories.collectLatest { categories ->
                defaultCategoryAdapter.submitList(categories)
                updateEmptyState()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.customCategoriesCount.collectLatest { count ->
                tvCustomCount.text = if (count == 1) "$count category" else "$count categories"
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.showDialog.collectLatest { dialogType ->
                when (dialogType) {
                    CategoriesViewModel.DialogType.NEW_CATEGORY -> showNewCategoryDialog()
                    CategoriesViewModel.DialogType.EDIT_CATEGORY -> showEditCategoryDialog()
                    null -> dismissDialogs()
                }
            }
        }
    }

    private fun setupClickListeners() {
        btnNewCategory.setOnClickListener {
            viewModel.showNewCategoryDialog()
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun updateEmptyState() {
        val hasCustomCategories = customCategoryAdapter.itemCount > 0
        val hasDefaultCategories = defaultCategoryAdapter.itemCount > 0

        if (!hasCustomCategories && !hasDefaultCategories) {
            emptyState.visibility = View.VISIBLE
            customCategoriesCard.visibility = View.GONE
            defaultCategoriesCard.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            customCategoriesCard.visibility = if (hasCustomCategories) View.VISIBLE else View.GONE
            defaultCategoriesCard.visibility = View.VISIBLE
        }
    }

    private fun onCategoryClicked(category: Category) {
        showCategoryOptionsDialog(category)
    }

    private fun showCategoryOptionsDialog(category: Category) {
        val options = if (category.isCustom) {
            arrayOf("Add Entry", "View Entries", "Edit Category", "Delete Category")
        } else {
            arrayOf("Add Entry", "View Entries", if (category.isPremium) "Upgrade to Edit" else "Edit Category")
        }

        AlertDialog.Builder(this)
            .setTitle(category.name)
            .setItems(options) { dialog, which ->
                when {
                    which == 0 -> {
                        viewModel.incrementEntryCount(category.id)
                        Toast.makeText(this, "Entry added to ${category.name}", Toast.LENGTH_SHORT).show()
                    }
                    which == 1 -> openCategoryEntries(category)
                    which == 2 && category.isCustom -> viewModel.showEditCategoryDialog(category)
                    which == 2 && !category.isCustom && category.isPremium -> showPremiumRequiredDialog()
                    which == 2 && !category.isCustom -> viewModel.showEditCategoryDialog(category)
                    which == 3 && category.isCustom -> showDeleteConfirmationDialog(category)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPremiumRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Premium Required")
            .setMessage("Upgrade to premium to edit default categories and unlock all features!")
            .setPositiveButton("Upgrade") { dialog, which ->
                Toast.makeText(this, "Navigate to Premium", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun openCategoryEntries(category: Category) {
        Toast.makeText(this, "Opening ${category.name} entries", Toast.LENGTH_SHORT).show()
    }

    private fun showNewCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreate)

        newCategoryDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Create New Category")
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            viewModel.dismissDialog()
        }

        btnCreate.setOnClickListener {
            val categoryName = etCategoryName.text?.toString()?.trim()
            if (categoryName.isNullOrEmpty()) {
                etCategoryName.error = "Category name is required"
                return@setOnClickListener
            }

            if (categoryName.length < 2) {
                etCategoryName.error = "Category name must be at least 2 characters"
                return@setOnClickListener
            }

            viewModel.createCategory(categoryName)
        }

        etCategoryName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etCategoryName.error = null
            }
        }

        newCategoryDialog?.setOnDismissListener {
            viewModel.dismissDialog()
        }

        newCategoryDialog?.show()
        etCategoryName.requestFocus()
    }

    private fun showEditCategoryDialog() {
        val category = viewModel.selectedCategory.value ?: return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        etCategoryName.setText(category.name)

        editCategoryDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Category")
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            viewModel.dismissDialog()
        }

        btnSave.setOnClickListener {
            val categoryName = etCategoryName.text?.toString()?.trim()
            when {
                categoryName.isNullOrEmpty() -> {
                    etCategoryName.error = "Category name is required"
                }
                categoryName.length < 2 -> {
                    etCategoryName.error = "Category name must be at least 2 characters"
                }
                else -> {
                    viewModel.updateCategory(category.id, categoryName)
                }
            }
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog(category)
        }

        etCategoryName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etCategoryName.error = null
            }
        }

        editCategoryDialog?.setOnDismissListener {
            viewModel.dismissDialog()
        }

        editCategoryDialog?.show()
        etCategoryName.requestFocus()
        etCategoryName.setSelection(0, etCategoryName.text?.length ?: 0)
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'? All entries in this category will also be deleted. This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, which ->
                viewModel.deleteCategory(category.id)
                Toast.makeText(this, "'${category.name}' deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun dismissDialogs() {
        newCategoryDialog?.dismiss()
        editCategoryDialog?.dismiss()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialogs()
    }
}