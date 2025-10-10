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
import com.javid.habitify.ProfileActivity
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
    private lateinit var selectedDateLayout: LinearLayout
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnAddForDate: Button

    private lateinit var navMain: TextView
    private lateinit var navHabits: TextView
    private lateinit var navMood: TextView
    private lateinit var navCategories: TextView

    private lateinit var toolbarTitle: TextView
    private lateinit var toolbarSearch: ImageButton
    private lateinit var toolbarNotifications: ImageButton
    private lateinit var toolbarSettings: ImageButton
    private lateinit var toolbarProfile: ImageButton

    private val habitsList = mutableListOf<Habit>()
    private var currentDialog: AlertDialog? = null
    private lateinit var prefsManager: PrefsManager
    private val gson = Gson()

    private var selectedDate: Date = Date()
    private val dateFormatDisplay = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val dateFormatStorage = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
//        setupBottomNavigation()
        updateSelectedDateDisplay()
        loadHabitsForSelectedDate()
        updateUI()

        requestNotificationPermission()
        HabitReminderService.startService(requireContext())
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

        selectedDateLayout = view.findViewById(R.id.selectedDateLayout)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        btnAddForDate = view.findViewById(R.id.btnAddForDate)

        navMain = view.findViewById(R.id.navMain)
        navHabits = view.findViewById(R.id.navHabits)
        navMood = view.findViewById(R.id.navMood)
        navCategories = view.findViewById(R.id.navCategories)

        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        toolbarSearch = view.findViewById(R.id.toolbarSearch)
        toolbarNotifications = view.findViewById(R.id.toolbarNotifications)
        toolbarSettings = view.findViewById(R.id.toolbarSettings)
        toolbarProfile = view.findViewById(R.id.toolbarProfile)
    }

    private fun setupToolbar() {
        toolbarTitle.text = "Today"
        toolbarSearch.setOnClickListener { showSearchDialog() }
        toolbarNotifications.setOnClickListener { showNotifications() }
        toolbarSettings.setOnClickListener { showSettings() }
        toolbarProfile.setOnClickListener { showProfile() }
    }

    private fun loadHabitsForSelectedDate() {
        habitsList.clear()

        val allHabitsJson = prefsManager.getUserPreference("user_habits", "")
        if (allHabitsJson.isNotEmpty()) {
            try {
                val type = object : TypeToken<Map<String, List<Habit>>>() {}.type
                val allHabitsMap = gson.fromJson<Map<String, List<Habit>>>(allHabitsJson, type) ?: emptyMap()

                val selectedDateKey = dateFormatStorage.format(selectedDate)
                val habitsForDate = allHabitsMap[selectedDateKey] ?: emptyList()

                habitsList.addAll(habitsForDate)
                Log.d("HomeFragment", "Loaded ${habitsList.size} habits for $selectedDateKey")
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

    private fun saveHabitsForSelectedDate() {
        try {
            val allHabitsJson = prefsManager.getUserPreference("user_habits", "")
            val type = object : TypeToken<Map<String, List<Habit>>>() {}.type
            val allHabitsMap = if (allHabitsJson.isNotEmpty()) {
                gson.fromJson<MutableMap<String, List<Habit>>>(allHabitsJson, type) ?: mutableMapOf()
            } else {
                mutableMapOf()
            }

            val selectedDateKey = dateFormatStorage.format(selectedDate)
            if (habitsList.isEmpty()) {
                allHabitsMap.remove(selectedDateKey)
            } else {
                allHabitsMap[selectedDateKey] = habitsList.toList()
            }

            val updatedHabitsJson = gson.toJson(allHabitsMap)
            prefsManager.setUserPreference("user_habits", updatedHabitsJson)
            Log.d("HomeFragment", "Saved ${habitsList.size} habits for $selectedDateKey")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error saving habits: ${e.message}")
        }
    }

    private fun addHabit(habit: Habit) {
        val allHabitsJson = prefsManager.getUserPreference("user_habits", "")
        val type = object : TypeToken<Map<String, List<Habit>>>() {}.type
        val allHabitsMap = if (allHabitsJson.isNotEmpty()) {
            gson.fromJson<MutableMap<String, List<Habit>>>(allHabitsJson, type) ?: mutableMapOf()
        } else {
            mutableMapOf()
        }

        val dateKey = habit.date
        val habitsForDate = allHabitsMap[dateKey]?.toMutableList() ?: mutableListOf()
        habitsForDate.add(habit)
        allHabitsMap[dateKey] = habitsForDate

        val updatedHabitsJson = gson.toJson(allHabitsMap)
        prefsManager.setUserPreference("user_habits", updatedHabitsJson)

        if (dateKey == dateFormatStorage.format(selectedDate)) {
            habitsList.add(habit)
            updateUI()
        }

        if (habit.isReminderEnabled) {
            scheduleHabitReminder(habit)
        }

        val habitDate = dateFormatStorage.parse(habit.date) ?: Date()
        showToast("Habit '${habit.name}' added for ${dateFormatDisplay.format(habitDate)}!")
    }

    private fun deleteHabit(habit: Habit) {
        if (habit.isReminderEnabled) {
            cancelHabitReminder(habit.id)
        }

        val allHabitsJson = prefsManager.getUserPreference("user_habits", "")
        val type = object : TypeToken<Map<String, List<Habit>>>() {}.type
        val allHabitsMap = if (allHabitsJson.isNotEmpty()) {
            gson.fromJson<MutableMap<String, List<Habit>>>(allHabitsJson, type) ?: mutableMapOf()
        } else {
            mutableMapOf()
        }

        val dateKey = habit.date
        val habitsForDate = allHabitsMap[dateKey]?.toMutableList()
        habitsForDate?.removeAll { it.id == habit.id }

        if (habitsForDate.isNullOrEmpty()) {
            allHabitsMap.remove(dateKey)
        } else {
            allHabitsMap[dateKey] = habitsForDate
        }

        val updatedHabitsJson = gson.toJson(allHabitsMap)
        prefsManager.setUserPreference("user_habits", updatedHabitsJson)

        if (dateKey == dateFormatStorage.format(selectedDate)) {
            habitsList.removeAll { it.id == habit.id }
            updateUI()
        }

        showToast("Habit '${habit.name}' deleted!")
    }

    private fun updateHabitCompletion(habit: Habit, isCompleted: Boolean) {
        val index = habitsList.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habitsList[index] = habit.copy(completed = isCompleted)
            saveHabitsForSelectedDate()
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

//    private fun setupBottomNavigation() {
//        setBottomNavSelected(navMain)
//
//        navCategories.setOnClickListener {
//            setBottomNavSelected(navCategories)
//            val intent = Intent(requireContext(), CategoriesActivity::class.java)
//            startActivity(intent)
//            showToast("Categories")
//        }
//
//        navMain.setOnClickListener {
//            setBottomNavSelected(navMain)
//            val intent = Intent(requireContext(), MoodJournalActivity::class.java)
//            startActivity(intent)
//            showToast("Main Activity")
//        }
//
//        navHabits.setOnClickListener {
//            setBottomNavSelected(navHabits)
//            navigateToHabits()
//        }
//
//        navMood.setOnClickListener {
//            setBottomNavSelected(navMood)
//            navigateToMoodJournal()
//        }
//    }


//    private fun setBottomNavSelected(selectedView: TextView) {
//        val navItems = listOf(navMain, navHabits, navMood)
//        navItems.forEach { item ->
//            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
//            item.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
//        }
//
//        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
//        selectedView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_selected_bg))
//    }

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

            if (isSameDay(calendar.time, selectedDate)) {
                dayView.setBackgroundResource(R.drawable.calendar_selected_bg)
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

        btnAddForDate.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun onDaySelected(date: Date) {
        selectedDate = date
        updateSelectedDateDisplay()
        setupCalendar()
        loadHabitsForSelectedDate()

        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        showToast("Selected: ${dateFormat.format(date)}")
    }

    private fun selectToday() {
        selectedDate = Date()
        updateSelectedDateDisplay()
        setupCalendar()
        loadHabitsForSelectedDate()
        showToast("Showing today's habits")
    }

    private fun updateSelectedDateDisplay() {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { time = selectedDate }

        val displayText = if (isSameDay(today, selected)) {
            "Today, ${dateFormatDisplay.format(selectedDate)}"
        } else {
            dateFormatDisplay.format(selectedDate)
        }

        tvSelectedDate.text = displayText
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit_with_date, null)

        val etHabitName: EditText = dialogView.findViewById(R.id.etHabitName)
        val spSchedule: Spinner = dialogView.findViewById(R.id.spSchedule)
        val switchReminder: Switch = dialogView.findViewById(R.id.switchReminder)
        val etReminderTime: EditText = dialogView.findViewById(R.id.etReminderTime)
        val reminderTimeLayout: LinearLayout = dialogView.findViewById(R.id.reminderTimeLayout)
        val btnSelectDate: Button = dialogView.findViewById(R.id.btnSelectDate1)
        val tvSelectedDateDialog: TextView = dialogView.findViewById(R.id.tvSelectedDate1)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnAdd: Button = dialogView.findViewById(R.id.btnAdd)

        val schedules = arrayOf("Daily", "Weekdays", "Weekends", "Mon, Wed, Fri", "Tue, Thu", "Custom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, schedules)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSchedule.adapter = adapter

        var habitDate = selectedDate
        tvSelectedDateDialog.text = dateFormatDisplay.format(habitDate)

        reminderTimeLayout.visibility = if (switchReminder.isChecked) View.VISIBLE else View.GONE

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderTimeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        etReminderTime.setOnClickListener {
            showTimePicker(etReminderTime)
        }

        btnSelectDate.setOnClickListener {
            showDatePickerForHabit { selectedDate ->
                habitDate = selectedDate
                tvSelectedDateDialog.text = dateFormatDisplay.format(habitDate)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Habit")
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
                completed = false,
                date = dateFormatStorage.format(habitDate) // Use the selected date
            )

            addHabit(newHabit)
            dialog.dismiss()
            currentDialog = null
        }

        dialog.show()
        currentDialog = dialog
    }

    private fun showDatePickerForHabit(onDateSelected: (Date) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_select_date, null)

        val calendarView: CalendarView = dialogView.findViewById(R.id.calendarView)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnSelect: Button = dialogView.findViewById(R.id.btnSelect)

        var selectedDate = Date()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Date for Habit")
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSelect.setOnClickListener {
            onDateSelected(selectedDate)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHabitOptions(habit: Habit) {
        val options = if (habit.isReminderEnabled) {
            arrayOf("Edit", "Delete", "View Statistics", "Disable Reminder", "Move to Another Day")
        } else {
            arrayOf("Edit", "Delete", "View Statistics", "Enable Reminder", "Move to Another Day")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(habit.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editHabit(habit)
                    1 -> showDeleteConfirmation(habit)
                    2 -> showToast("Statistics for ${habit.name}")
                    3 -> toggleHabitReminder(habit)
                    4 -> showMoveHabitDialog(habit)
                }
            }
            .show()
    }

    private fun showMoveHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_select_date, null)

        val calendarView: CalendarView = dialogView.findViewById(R.id.calendarView)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnSelect: Button = dialogView.findViewById(R.id.btnSelect) // Changed from btnMove to btnSelect

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Move '${habit.name}' to Another Day")
            .create()

        var selectedMoveDate = Date()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedMoveDate = calendar.time
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSelect.setOnClickListener { // Changed from btnMove to btnSelect
            moveHabitToDate(habit, selectedMoveDate)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun moveHabitToDate(habit: Habit, newDate: Date) {
        deleteHabit(habit)

        val newHabit = habit.copy(
            date = dateFormatStorage.format(newDate)
        )

        addHabit(newHabit)
        showToast("Habit moved to ${dateFormatDisplay.format(newDate)}")
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
        // Implement edit functionality here
    }

    private fun toggleHabitReminder(habit: Habit) {
        if (habit.isReminderEnabled) {
            cancelHabitReminder(habit.id)
            val index = habitsList.indexOfFirst { it.id == habit.id }
            if (index != -1) {
                habitsList[index] = habit.copy(isReminderEnabled = false, reminderTime = null)
                saveHabitsForSelectedDate()
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
                    saveHabitsForSelectedDate()
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

            val firstTriggerTime = calculateFirstTriggerTime(hour, minute)

            Log.d("HabitReminder", "Scheduling alarm for ${habit.name} at ${Date(firstTriggerTime)}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        firstTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        firstTriggerTime,
                        pendingIntent
                    )
                    Log.w("HabitReminder", "Using inexact alarm due to permission restrictions")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerTime,
                    pendingIntent
                )
            }

            Log.d("HabitReminder", "Alarm scheduled for: ${habit.name} at ${habit.reminderTime} (first trigger: ${Date(firstTriggerTime)})")
            showToast("⏰ Reminder set for ${habit.reminderTime}")

        } catch (e: SecurityException) {
            Log.e("HabitReminder", "Security Exception: ${e.message}")
            showToast("Cannot schedule exact alarms. Please check app permissions.")
        } catch (e: Exception) {
            Log.e("HabitReminder", "Failed to schedule alarm: ${e.message}")
            showToast("Failed to schedule reminder: ${e.message}")
        }
    }

    private fun calculateFirstTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
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

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return isSameDay(cal1, cal2)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

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
        val intent = Intent(requireContext(), ProfileActivity::class.java)
        val sp = PrefsManager(requireContext())
        val bundle : Bundle = Bundle().apply {
            putString("name",sp.getCurrentUser()?.username)
            putString("id",sp.getCurrentUser()?.id)
            putString("email",sp.getCurrentUser()?.email)
        }
        intent.putExtra("user",bundle)
        startActivity(intent)
        showToast("Profile")
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
                } else {
                    showToast("Notification permission denied. Reminders won't work.")
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}