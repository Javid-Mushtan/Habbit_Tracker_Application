package com.javid.habitify.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.javid.habitify.R
import com.javid.habitify.model.Habit
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var calendarContainer: LinearLayout
    private lateinit var habitsContainer: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAdd: ImageButton
    private lateinit var bottomNavigationView: LinearLayout

    // Bottom nav items
    private lateinit var navPremium: TextView
    private lateinit var navToday: TextView
    private lateinit var navHabits: TextView
    private lateinit var navTasks: TextView
    private lateinit var navCategories: TextView
    private lateinit var navTimer: TextView

    // Toolbar items
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbarSearch: ImageButton
    private lateinit var toolbarNotifications: ImageButton
    private lateinit var toolbarSettings: ImageButton
    private lateinit var toolbarProfile: ImageButton

    private val habitsList = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupToolbar()
        setupCalendar()
        setupClickListeners()
        setupBottomNavigation()
        loadHabits()
        updateUI()
    }

    private fun initViews(view: View) {
        calendarContainer = view.findViewById(R.id.calendarContainer)
        habitsContainer = view.findViewById(R.id.habitsContainer)
        emptyState = view.findViewById(R.id.emptyState)
        fabAdd = view.findViewById(R.id.fabAdd)
        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)

        // Bottom nav items
        navPremium = view.findViewById(R.id.navPremium)
        navToday = view.findViewById(R.id.navToday)
        navHabits = view.findViewById(R.id.navHabits)
        navTasks = view.findViewById(R.id.navTasks)
        navCategories = view.findViewById(R.id.navCategories)
        navTimer = view.findViewById(R.id.navTimer)

        // Toolbar items
        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        toolbarSearch = view.findViewById(R.id.toolbarSearch)
        toolbarNotifications = view.findViewById(R.id.toolbarNotifications)
        toolbarSettings = view.findViewById(R.id.toolbarSettings)
        toolbarProfile = view.findViewById(R.id.toolbarProfile)
    }

    private fun setupToolbar() {
        toolbarTitle.text = "Today"

        toolbarSearch.setOnClickListener {
            showSearchDialog()
        }

        toolbarNotifications.setOnClickListener {
            showNotifications()
        }

        toolbarSettings.setOnClickListener {
            showSettings()
        }

        toolbarProfile.setOnClickListener {
            showProfile()
        }
    }

    private fun setupBottomNavigation() {
        // Set Today as selected by default
        setBottomNavSelected(navToday)

        navPremium.setOnClickListener {
            setBottomNavSelected(navPremium)
            showToast("Premium clicked")
        }

        navToday.setOnClickListener {
            setBottomNavSelected(navToday)
            loadHabits()
        }

        navHabits.setOnClickListener {
            setBottomNavSelected(navHabits)
            navigateToHabits()
        }

        navTasks.setOnClickListener {
            setBottomNavSelected(navTasks)
            navigateToTasks()
        }

        navCategories.setOnClickListener {
            setBottomNavSelected(navCategories)
            navigateToCategories()
        }

        navTimer.setOnClickListener {
            setBottomNavSelected(navTimer)
            navigateToTimer()
        }
    }

    private fun setBottomNavSelected(selectedView: TextView) {
        // Reset all to unselected state
        val navItems = listOf(navPremium, navToday, navHabits, navTasks, navCategories, navTimer)
        navItems.forEach { item ->
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
            item.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }

        // Set selected state
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
        selectedView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_selected_bg))
    }

    private fun setupCalendar() {
        calendarContainer.removeAllViews()

        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 0 until 7) {
            val dayView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_calendar, calendarContainer, false)

            val tvDate: TextView = dayView.findViewById(R.id.tvDate)
            val tvDay: TextView = dayView.findViewById(R.id.tvDay)
            val indicator: View = dayView.findViewById(R.id.indicator)

            val date = calendar.time
            tvDate.text = dateFormat.format(date)
            tvDay.text = dayFormat.format(date)

            // Highlight today
            val today = Calendar.getInstance()
            if (isSameDay(calendar, today)) {
                dayView.setBackgroundResource(R.drawable.calendar_today_bg)
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                tvDay.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }

            // Add click listener
            dayView.setOnClickListener {
                onDaySelected(calendar.time)
            }

            calendarContainer.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun setupClickListeners() {
        fabAdd.setOnClickListener {
            showAddHabitDialog()
        }
    }

    // Menu Action Methods
    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_search, null)

        val etSearch: EditText = dialogView.findViewById(R.id.etSearch)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnSearch: Button = dialogView.findViewById(R.id.btnSearch)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Search Habits")
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchHabits(query)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showNotifications() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notifications")
            .setMessage("You have no new notifications")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("Settings")
            .setItems(arrayOf("Account", "Theme", "Notifications", "Backup", "About")) { _, which ->
                when (which) {
                    0 -> showToast("Account Settings")
                    1 -> showToast("Theme Settings")
                    2 -> showToast("Notification Settings")
                    3 -> showToast("Backup Settings")
                    4 -> showToast("About Habitify")
                }
            }
            .show()
    }

    private fun showProfile() {
        AlertDialog.Builder(requireContext())
            .setTitle("Profile")
            .setMessage("User Profile Information\n\nName: John Doe\nEmail: john@example.com\nMember since: 2024")
            .setPositiveButton("Edit Profile") { _, _ ->
                showToast("Edit Profile")
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // Navigation Methods
    private fun navigateToHabits() {
        showToast("Navigating to Habits Management")
    }

    private fun navigateToTasks() {
        showToast("Navigating to Tasks")
    }

    private fun navigateToCategories() {
        showToast("Navigating to Categories")
    }

    private fun navigateToTimer() {
        showToast("Navigating to Timer")
    }

    private fun loadHabits() {
        habitsList.clear()

        // Sample data
        habitsList.addAll(listOf(
            Habit("Morning Meditation", "Daily", completed = true),
            Habit("Exercise", "Mon, Wed, Fri", completed = false),
            Habit("Read Book", "Daily", completed = false),
            Habit("Drink Water", "Every 2 hours", completed = true),
            Habit("Learn Kotlin", "Daily", completed = true)
        ))

        updateUI()
    }

    private fun updateUI() {
        if (habitsList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            habitsContainer.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            habitsContainer.visibility = View.VISIBLE
            populateHabitsList()
        }
    }

    private fun populateHabitsList() {
        habitsContainer.removeAllViews()

        habitsList.forEach { habit ->
            val habitView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_habit, habitsContainer, false)

            val tvHabitName: TextView = habitView.findViewById(R.id.tvHabitName)
            val tvHabitSchedule: TextView = habitView.findViewById(R.id.tvHabitSchedule)
            val cbCompleted: CheckBox = habitView.findViewById(R.id.cbCompleted)
            val btnOptions: ImageButton = habitView.findViewById(R.id.btnOptions)

            tvHabitName.text = habit.name
            tvHabitSchedule.text = habit.schedule
            cbCompleted.isChecked = habit.completed

            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                habit.completed = isChecked
                showToast("${habit.name} ${if (isChecked) "completed" else "pending"}")
            }

            btnOptions.setOnClickListener {
                showHabitOptions(habit)
            }

            habitsContainer.addView(habitView)
        }
    }

    private fun searchHabits(query: String) {
        val filteredHabits = habitsList.filter {
            it.name.contains(query, ignoreCase = true)
        }

        if (filteredHabits.isEmpty()) {
            showToast("No habits found for '$query'")
        } else {
            showToast("Found ${filteredHabits.size} habits")
        }
    }

    private fun onDaySelected(date: Date) {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        showToast("Selected: ${dateFormat.format(date)}")
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)

        val etHabitName: EditText = dialogView.findViewById(R.id.etHabitName)
        val spSchedule: Spinner = dialogView.findViewById(R.id.spSchedule)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnAdd: Button = dialogView.findViewById(R.id.btnAdd)

        val schedules = arrayOf("Daily", "Weekdays", "Weekends", "Mon, Wed, Fri", "Tue, Thu", "Custom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, schedules)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSchedule.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add New Habit")
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnAdd.setOnClickListener {
            val habitName = etHabitName.text.toString().trim()
            val schedule = spSchedule.selectedItem.toString()

            if (habitName.isEmpty()) {
                etHabitName.error = "Please enter habit name"
                return@setOnClickListener
            }

            val newHabit = Habit(habitName, schedule, completed = false)
            habitsList.add(newHabit)
            updateUI()
            dialog.dismiss()
            showToast("Habit '$habitName' added!")
        }

        dialog.show()
    }

    private fun showHabitOptions(habit: Habit) {
        val options = arrayOf("Edit", "Delete", "View Statistics", "Change Schedule")

        AlertDialog.Builder(requireContext())
            .setTitle(habit.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editHabit(habit)
                    1 -> deleteHabit(habit)
                    2 -> showToast("Statistics for ${habit.name}")
                    3 -> changeHabitSchedule(habit)
                }
            }
            .show()
    }

    private fun editHabit(habit: Habit) {
        showToast("Editing ${habit.name}")
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                habitsList.remove(habit)
                updateUI()
                showToast("Habit deleted")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeHabitSchedule(habit: Habit) {
        showToast("Change schedule for ${habit.name}")
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}