package com.javid.habitify.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.javid.habitify.R
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

    private var dailyGoal = 2000
    private var currentIntake = 0
    private val todayLogs = mutableListOf<WaterLog>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_water_tracker, container, false)

        initializeViews(view)
        setupClickListeners()
        updateUI()

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
    }

    private fun setupClickListeners() {
        btnSetGoal.setOnClickListener {
            setDailyGoal()
        }

        btn100ml.setOnClickListener {
            logWater(100)
        }

        btn200ml.setOnClickListener {
            logWater(200)
        }

        btn300ml.setOnClickListener {
            logWater(300)
        }

        btnCustom.setOnClickListener {
            showCustomAmountDialog()
        }
    }

    private fun setDailyGoal() {
        val goalText = etDailyGoal.text.toString()
        if (goalText.isNotEmpty()) {
            val newGoal = goalText.toInt()
            if (newGoal > 0) {
                dailyGoal = newGoal
                etDailyGoal.text.clear()
                updateUI()
                showToast("Daily goal set to $newGoal ml")
            } else {
                showToast("Please enter a positive number")
            }
        } else {
            showToast("Please enter a daily goal")
        }
    }

    private fun logWater(amount: Int) {
        currentIntake += amount
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        todayLogs.add(WaterLog(amount, currentTime))

        updateUI()
        showToast("Logged $amount ml of water")
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
                        logWater(amount)
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

    private fun updateUI() {
        tvCurrentGoal.text = "Current goal: $dailyGoal ml"

        val progress = min(currentIntake.toFloat() / dailyGoal, 1.0f)
        tvProgress.text = "$currentIntake/$dailyGoal ml"
        tvRemaining.text = "Remaining: ${dailyGoal - currentIntake} ml"

        updateProgressCircle(progress)

        updateLogsDisplay()
    }

    private fun updateProgressCircle(progress: Float) {
        val scale = 0.3f + (0.7f * progress)
        progressCircle.scaleX = scale
        progressCircle.scaleY = scale
    }

    private fun updateLogsDisplay() {
        logsContainer.removeAllViews()

        if (todayLogs.isEmpty()) {
            tvNoLogs.visibility = View.VISIBLE
            logsContainer.visibility = View.GONE
        } else {
            tvNoLogs.visibility = View.GONE
            logsContainer.visibility = View.VISIBLE

            todayLogs.reversed().forEach { log ->
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

    data class WaterLog(val amount: Int, val time: String)

    companion object {
        fun newInstance(): WaterTrackerFragment {
            return WaterTrackerFragment()
        }
    }
}