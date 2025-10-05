package com.javid.habitify.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.javid.habitify.R
import com.javid.habitify.model.WaterLog
import com.javid.habitify.services.WaterReminderService
import com.javid.habitify.services.WaterResetService
import com.javid.habitify.utils.PrefsManager
import com.javid.habitify.viewmodel.WaterTrackerViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class WaterTrackerFragment : Fragment() {

    private lateinit var etDailyGoal: EditText
    private lateinit var btnSetGoal: Button
    private lateinit var tvCurrentGoal: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var progressCircle: View
    private lateinit var btn100ml: Button
    private lateinit var btn200ml: Button
    private lateinit var btn300ml: Button
    private lateinit var btnCustom: Button
    private lateinit var logsContainer: LinearLayout
    private lateinit var tvNoLogs: TextView
    private lateinit var goalCompletedView: CardView
    private lateinit var btnResetToday: Button
    private lateinit var btnToggleReminders: Button
    private lateinit var remindersStatusView: CardView
    private lateinit var tvRemindersStatus: TextView

    private val viewModel: WaterTrackerViewModel by viewModels()
    private lateinit var prefsManager: PrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_water_tracker, container, false)
        prefsManager = PrefsManager(requireContext())

        initializeViews(view)
        setupClickListeners()
        setupObservers()

        viewModel.initialize(prefsManager)

        WaterResetService.scheduleDailyReset(requireContext())

        return view
    }

    private fun initializeViews(view: View) {
        etDailyGoal = view.findViewById(R.id.etDailyGoal)
        btnSetGoal = view.findViewById(R.id.btnSetGoal)
        tvCurrentGoal = view.findViewById(R.id.tvCurrentGoal)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvRemaining = view.findViewById(R.id.tvRemaining)
        progressCircle = view.findViewById(R.id.progressCircle)
        btn100ml = view.findViewById(R.id.btn100ml)
        btn200ml = view.findViewById(R.id.btn200ml)
        btn300ml = view.findViewById(R.id.btn300ml)
        btnCustom = view.findViewById(R.id.btnCustom)
        logsContainer = view.findViewById(R.id.logsContainer)
        tvNoLogs = view.findViewById(R.id.tvNoLogs)

        goalCompletedView = view.findViewById(R.id.goalCompletedView)
        btnResetToday = view.findViewById(R.id.btnResetToday)

        // Add these new views for reminders
        btnToggleReminders = view.findViewById(R.id.btnToggleReminders)
        remindersStatusView = view.findViewById(R.id.remindersStatusView)
        tvRemindersStatus = view.findViewById(R.id.tvRemindersStatus)
    }

    private fun setupObservers() {
        viewModel.dailyGoal.observe(viewLifecycleOwner) { goal ->
            tvCurrentGoal.text = "Current goal: $goal ml"
            updateProgressDisplay()
            // Check if we should schedule reminders when goal changes
            checkAndScheduleReminders()
        }

        viewModel.currentIntake.observe(viewLifecycleOwner) { intake ->
            updateProgressDisplay()
            // Check if we should schedule/cancel reminders when intake changes
            checkAndScheduleReminders()
        }

        viewModel.todayLogs.observe(viewLifecycleOwner) { logs ->
            updateLogsDisplay(logs)
        }

        viewModel.isGoalCompleted.observe(viewLifecycleOwner) { completed ->
            goalCompletedView.visibility = if (completed) View.VISIBLE else View.GONE
            if (completed) {
                showToast("🎉 Daily water goal completed!")
                // Cancel reminders when goal is completed
                WaterReminderService.cancelAllReminders(requireContext())
                updateRemindersStatus()
            }
        }

        // Update reminders status initially
        updateRemindersStatus()
    }

    private fun setupClickListeners() {
        btnSetGoal.setOnClickListener {
            setDailyGoal()
        }

        btn100ml.setOnClickListener {
            viewModel.addWaterLog(100)
            showToast("Logged 100 ml of water")
        }

        btn200ml.setOnClickListener {
            viewModel.addWaterLog(200)
            showToast("Logged 200 ml of water")
        }

        btn300ml.setOnClickListener {
            viewModel.addWaterLog(300)
            showToast("Logged 300 ml of water")
        }

        btnCustom.setOnClickListener {
            showCustomAmountDialog()
        }

        btnResetToday.setOnClickListener {
            showResetConfirmation()
        }

        btnToggleReminders.setOnClickListener {
            toggleReminders()
        }
    }

    private fun setDailyGoal() {
        val goalText = etDailyGoal.text.toString()
        if (goalText.isNotEmpty()) {
            val newGoal = goalText.toInt()
            if (newGoal > 0) {
                viewModel.setDailyGoal(newGoal)
                etDailyGoal.text.clear()
                showToast("Daily goal set to $newGoal ml")
            } else {
                showToast("Please enter a positive number")
            }
        } else {
            showToast("Please enter a daily goal")
        }
    }

    private fun showCustomAmountDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_water, null)
        val etCustomAmount = dialogView.findViewById<EditText>(R.id.etCustomAmount)

        AlertDialog.Builder(requireContext())
            .setTitle("Custom Water Amount")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val amountText = etCustomAmount.text.toString()
                if (amountText.isNotEmpty()) {
                    val amount = amountText.toInt()
                    if (amount > 0) {
                        viewModel.addWaterLog(amount)
                        showToast("Logged $amount ml of water")
                    } else {
                        showToast("Please enter a positive number")
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Today's Data")
            .setMessage("Are you sure you want to reset today's water intake?")
            .setPositiveButton("Reset") { dialog, _ ->
                viewModel.clearTodayData()
                showToast("Today's data reset")
                // Cancel reminders when data is reset
                WaterReminderService.cancelAllReminders(requireContext())
                updateRemindersStatus()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toggleReminders() {
        if (WaterReminderService.areRemindersActive(requireContext())) {
            WaterReminderService.cancelAllReminders(requireContext())
            showToast("🔕 Hourly reminders disabled")
        } else {
            // Schedule reminders if goal is not completed
            val goal = viewModel.dailyGoal.value ?: 2000
            val intake = viewModel.currentIntake.value ?: 0
            if (intake < goal) {
                WaterReminderService.scheduleReminders(requireContext())
                showToast("🔔 Hourly reminders enabled")
            } else {
                showToast("🎯 Goal already completed - no reminders needed")
            }
        }
        updateRemindersStatus()
    }

    private fun checkAndScheduleReminders() {
        val goal = viewModel.dailyGoal.value ?: 2000
        val intake = viewModel.currentIntake.value ?: 0

        if (intake < goal) {
            // Auto-enable reminders if they're not active and goal is not completed
            if (!WaterReminderService.areRemindersActive(requireContext())) {
                WaterReminderService.scheduleReminders(requireContext())
                updateRemindersStatus()
            }
        } else {
            // Cancel reminders if goal is completed
            WaterReminderService.cancelAllReminders(requireContext())
            updateRemindersStatus()
        }
    }

    private fun updateRemindersStatus() {
        val remindersActive = WaterReminderService.areRemindersActive(requireContext())

        if (remindersActive) {
            remindersStatusView.visibility = View.VISIBLE
            tvRemindersStatus.text = "🔔 Hourly reminders active"
            btnToggleReminders.text = "Disable Reminders"
            btnToggleReminders.setBackgroundColor(resources.getColor(R.color.red, null))
        } else {
            remindersStatusView.visibility = View.GONE
            btnToggleReminders.text = "Enable Reminders"
            btnToggleReminders.setBackgroundColor(resources.getColor(R.color.primary_color, null))
        }
    }

    private fun updateProgressDisplay() {
        val goal = viewModel.dailyGoal.value ?: 2000
        val intake = viewModel.currentIntake.value ?: 0

        tvProgress.text = "$intake/$goal ml"
        tvRemaining.text = "Remaining: ${goal - intake} ml"

        val progress = min(intake.toFloat() / goal, 1.0f)
        updateProgressCircle(progress)
    }

    private fun updateProgressCircle(progress: Float) {
        val scale = 0.3f + (0.7f * progress)
        progressCircle.scaleX = scale
        progressCircle.scaleY = scale

        val color = when {
            progress >= 1.0f -> R.color.green_dark
            progress >= 0.7f -> R.color.water_dark
            progress >= 0.4f -> R.color.water_medium
            else -> R.color.water_light
        }
        progressCircle.setBackgroundColor(resources.getColor(color, null))
    }

    private fun updateLogsDisplay(logs: List<WaterLog>) {
        logsContainer.removeAllViews()

        if (logs.isEmpty()) {
            tvNoLogs.visibility = View.VISIBLE
            logsContainer.visibility = View.GONE
        } else {
            tvNoLogs.visibility = View.GONE
            logsContainer.visibility = View.VISIBLE

            logs.reversed().forEach { log ->
                val logView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_water_log, logsContainer, false)

                val tvAmount = logView.findViewById<TextView>(R.id.tvAmount)
                val tvTime = logView.findViewById<TextView>(R.id.tvTime)

                tvAmount.text = "+${log.amount} ml"
                tvTime.text = log.time

                logsContainer.addView(logView)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun newInstance(): WaterTrackerFragment {
            return WaterTrackerFragment()
        }
    }
}