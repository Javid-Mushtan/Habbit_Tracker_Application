package com.javid.habitify.fragments

import android.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.javid.habitify.CategoriesActivity
import com.javid.habitify.R
import com.javid.habitify.model.Habit
import com.javid.habitify.receivers.HabitReminderReceiver
import com.javid.habitify.services.HabitReminderService
import com.javid.habitify.utils.PrefsManager
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
    private var currentDialog: AlertDialog? = null
    private lateinit var prefsManager: PrefsManager
    private val gson = Gson()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefsManager = PrefsManager(requireContext())
    }

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

        requestNotificationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentDialog?.dismiss()
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

    // 🎯 DYNAMIC HABITS MANAGEMENT
    private fun loadHabits() {
        habitsList.clear()

        val savedHabits = prefsManager.getUserPreference("user_habits", "")
        if (savedHabits.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                val loadedHabits = gson.fromJson<List<Habit>>(savedHabits, type)
                habitsList.addAll(loadedHabits ?: emptyList())
                Log.d("HomeFragment", "Loaded ${habitsList.size} habits from storage")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading habits: ${e.message}")
                habitsList.clear()
            }
        } else {
            habitsList.clear()
            Log.d("HomeFragment", "No saved habits found")
        }

        updateUI()
    }

    private fun saveHabits() {
        try {
            val habitsJson = gson.toJson(habitsList)
            prefsManager.setUserPreference("user_habits", habitsJson)
            Log.d("HomeFragment", "Saved ${habitsList.size} habits to storage")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error saving habits: ${e.message}")
        }
    }

    private fun addHabit(habit: Habit) {
        habitsList.add(habit)
        saveHabits()
        updateUI()

        if (habit.isReminderEnabled) {
            scheduleHabitReminder(habit)
        }

        showToast("Habit '${habit.name}' added!")
    }

    private fun deleteHabit(habit: Habit) {
        if (habit.isReminderEnabled) {
            cancelHabitReminder(habit.id)
        }
        habitsList.remove(habit)
        saveHabits()
        updateUI()
        showToast("Habit '${habit.name}' deleted!")
    }

    private fun updateHabitCompletion(habit: Habit, isCompleted: Boolean) {
        val index = habitsList.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habitsList[index] = habit.copy(completed = isCompleted)
            saveHabits()
        }
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
            val reminderIcon: ImageView = habitView.findViewById(R.id.reminderIcon)

            tvHabitName.text = habit.name
            tvHabitSchedule.text = habit.schedule
            cbCompleted.isChecked = habit.completed

            if (habit.isReminderEnabled && !habit.reminderTime.isNullOrEmpty()) {
                reminderIcon.visibility = View.VISIBLE
                reminderIcon.contentDescription = "Reminder set for ${habit.reminderTime}"
            } else {
                reminderIcon.visibility = View.GONE
            }

            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                updateHabitCompletion(habit, isChecked)
                showToast("${habit.name} ${if (isChecked) "completed" else "pending"}")
            }

            btnOptions.setOnClickListener {
                showHabitOptions(habit)
            }

            habitsContainer.addView(habitView)
        }
    }

    private fun setupBottomNavigation() {
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
        val navItems = listOf(navPremium, navToday, navHabits, navTasks, navCategories, navTimer)
        navItems.forEach { item ->
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
            item.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }

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

        for (i in 0 until 30) {
            val dayView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_calendar, calendarContainer, false)

            val tvDate: TextView = dayView.findViewById(R.id.tvDate)
            val tvDay: TextView = dayView.findViewById(R.id.tvDay)
            val indicator: View = dayView.findViewById(R.id.indicator)

            val date = calendar.time
            tvDate.text = dateFormat.format(date)
            tvDay.text = dayFormat.format(date)

            val today = Calendar.getInstance()
            if (isSameDay(calendar, today)) {
                dayView.setBackgroundResource(R.drawable.calendar_today_bg)
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                tvDay.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }

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
            } else {
                etSearch.error = "Please enter search term"
            }
        }

        dialog.show()
        currentDialog = dialog
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

    private fun navigateToHabits() {
        showToast("Navigating to Habits Management")
    }

    private fun navigateToTasks() {
        showToast("Navigating to Tasks")
    }

    private fun navigateToCategories() {
        val intent = Intent(requireContext(), CategoriesActivity::class.java)
        startActivity(intent)
        showToast("Navigating to Categories")
    }

    private fun navigateToTimer() {
        showToast("Navigating to Timer")
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
        val switchReminder: Switch = dialogView.findViewById(R.id.switchReminder)
        val etReminderTime: EditText = dialogView.findViewById(R.id.etReminderTime)
        val reminderTimeLayout: LinearLayout = dialogView.findViewById(R.id.reminderTimeLayout)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnAdd: Button = dialogView.findViewById(R.id.btnAdd)

        val schedules = arrayOf("Daily", "Weekdays", "Weekends", "Mon, Wed, Fri", "Tue, Thu", "Custom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, schedules)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSchedule.adapter = adapter

        reminderTimeLayout.visibility = if (switchReminder.isChecked) View.VISIBLE else View.GONE

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderTimeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        etReminderTime.setOnClickListener {
            showTimePicker(etReminderTime)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add New Habit")
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
            currentDialog = null
        }

        btnAdd.setOnClickListener {
            val habitName = etHabitName.text.toString().trim()
            val schedule = spSchedule.selectedItem.toString()
            val isReminderEnabled = switchReminder.isChecked
            val reminderTime = if (isReminderEnabled) etReminderTime.text.toString() else null

            if (habitName.isEmpty()) {
                etHabitName.error = "Please enter habit name"
                return@setOnClickListener
            }

            if (isReminderEnabled && reminderTime.isNullOrEmpty()) {
                showToast("Please set reminder time")
                return@setOnClickListener
            }

            if (isReminderEnabled && !isValidTime(reminderTime!!)) {
                showToast("Please enter a valid time (HH:mm)")
                return@setOnClickListener
            }

            val newHabit = Habit(
                id = System.currentTimeMillis(),
                name = habitName,
                schedule = schedule,
                reminderTime = reminderTime,
                isReminderEnabled = isReminderEnabled,
                completed = false
            )

            addHabit(newHabit)
            dialog.dismiss()
            currentDialog = null
            testNotification()
        }

        dialog.show()
        currentDialog = dialog
    }

    private fun showHabitOptions(habit: Habit) {
        val options = if (habit.isReminderEnabled) {
            arrayOf("Edit", "Delete", "View Statistics", "Disable Reminder")
        } else {
            arrayOf("Edit", "Delete", "View Statistics", "Enable Reminder")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(habit.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editHabit(habit)
                    1 -> showDeleteConfirmation(habit)
                    2 -> showToast("Statistics for ${habit.name}")
                    3 -> toggleHabitReminder(habit)
                }
            }
            .show()
    }

    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editHabit(habit: Habit) {
        showToast("Editing ${habit.name}")
    }

    private fun toggleHabitReminder(habit: Habit) {
        if (habit.isReminderEnabled) {
            cancelHabitReminder(habit.id)
            val index = habitsList.indexOfFirst { it.id == habit.id }
            if (index != -1) {
                habitsList[index] = habit.copy(isReminderEnabled = false, reminderTime = null)
                saveHabits()
            }
            showToast("Reminder disabled for ${habit.name}")
        } else {
            showReminderTimePicker(habit)
        }
        updateUI()
    }

    private fun showReminderTimePicker(habit: Habit) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val timeString = String.format("%02d:%02d", hourOfDay, minute)

                val updatedHabit = habit.copy(
                    reminderTime = timeString,
                    isReminderEnabled = true
                )

                val index = habitsList.indexOfFirst { it.id == habit.id }
                if (index != -1) {
                    habitsList[index] = updatedHabit
                    scheduleHabitReminder(updatedHabit)
                    saveHabits()
                    updateUI()
                    showToast("Reminder set for $timeString")
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.setTitle("Set Reminder Time for ${habit.name}")
        timePicker.show()
    }

    private fun scheduleHabitReminder(habit: Habit) {
        if (!habit.isReminderEnabled || habit.reminderTime.isNullOrEmpty()) {
            return
        }

        if (!hasNotificationPermission()) {
            showToast("Please grant notification permission to set reminders")
            requestNotificationPermission()
            return
        }

        try {
            val (hour, minute) = parseTime(habit.reminderTime!!)
            val currentTime = Calendar.getInstance()
            val scheduledTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (before(currentTime)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(requireContext(), HabitReminderReceiver::class.java).apply {
                putExtra("habit_name", habit.name)
                putExtra("habit_id", habit.id)
                putExtra("hour", hour)
                putExtra("minute", minute)
                putExtra("is_daily", true)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                habit.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTime.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime.timeInMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime.timeInMillis,
                    pendingIntent
                )
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                scheduledTime.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )

            Log.d("HabitReminder", "Alarm scheduled for: ${habit.name} at ${habit.reminderTime}")
            showToast("⏰ Reminder set for ${habit.reminderTime}")

        } catch (e: SecurityException) {
            Log.e("HabitReminder", "Security Exception: ${e.message}")
            showToast("Cannot schedule exact alarms. Please check app permissions.")
        } catch (e: Exception) {
            Log.e("HabitReminder", "Failed to schedule alarm: ${e.message}")
            showToast("Failed to schedule reminder: ${e.message}")
        }
    }

    private fun cancelHabitReminder(habitId: Long) {
        try {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), HabitReminderReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                habitId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
                Log.d("HabitReminder", "Alarm cancelled for habit ID: $habitId")
            }

        } catch (e: Exception) {
            Log.e("HabitReminder", "Error cancelling alarm: ${e.message}")
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }

    private fun showTimePicker(etReminderTime: EditText) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val timeString = String.format("%02d:%02d", hourOfDay, minute)
                etReminderTime.setText(timeString)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    private fun isValidTime(time: String): Boolean {
        return time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$"))
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Notification permission methods
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Notification permission granted!")
                    HabitReminderService.startService(requireContext())
                    testNotification()
                } else {
                    showToast("Notification permission denied. Reminders won't work.")
                }
            }
        }
    }

    private fun testNotification() {
        if (!hasNotificationPermission()) {
            showToast("Please grant notification permission first")
            requestNotificationPermission()
            return
        }

        HabitReminderService.startService(requireContext())

        try {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), HabitReminderReceiver::class.java).apply {
                putExtra("habit_name", "Test Habit - Drink Water")
                putExtra("habit_id", 9999L)
                putExtra("test_notification", true)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val testTime = Calendar.getInstance().apply {
                add(Calendar.SECOND, 30)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        testTime.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    testTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    testTime.timeInMillis,
                    pendingIntent
                )
            }

            Log.d("HabitReminder", "Test alarm scheduled for 30 seconds from now")
            showToast("Test notification scheduled for 30 seconds from now")

        } catch (e: SecurityException) {
            Log.e("HabitReminder", "Security Exception: ${e.message}")
            showToast("Cannot schedule exact alarms. Please check app permissions.")
        } catch (e: Exception) {
            Log.e("HabitReminder", "Failed to schedule test alarm: ${e.message}")
            showToast("Failed to schedule test notification: ${e.message}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}