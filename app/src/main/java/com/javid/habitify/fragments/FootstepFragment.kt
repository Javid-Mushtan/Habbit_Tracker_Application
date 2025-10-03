package com.javid.habitify.fragments

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.javid.habitify.R

class FootstepFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    public var stepSensor: Sensor? = null

    private lateinit var tvStepCount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvGoalProgress: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvActiveTime: TextView
    private lateinit var tvGoalText: TextView
    private lateinit var etStepGoal: EditText
    private lateinit var btnSetGoal: Button
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button
    private lateinit var ivFootIcon: ImageView
    private lateinit var ivProgressArc: ImageView

    private var stepCount = 0
    private var dailyGoal = 10000
    private var isCounting = false
    private var startTime: Long = 0
    private var activeTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private var stepAnimationRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_footsteps, container, false)

        initializeViews(view)
        setupSensor()
        setupClickListeners()
        updateUI()

        return view
    }

    private fun initializeViews(view: View) {
        tvStepCount = view.findViewById(R.id.tvStepCount)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvGoalProgress = view.findViewById(R.id.tvGoalProgress)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvCalories = view.findViewById(R.id.tvCalories)
        tvActiveTime = view.findViewById(R.id.tvActiveTime)
        tvGoalText = view.findViewById(R.id.tvGoalText)
        etStepGoal = view.findViewById(R.id.etStepGoal)
        btnSetGoal = view.findViewById(R.id.btnSetGoal)
        btnStart = view.findViewById(R.id.btnStart)
        btnReset = view.findViewById(R.id.btnReset)
        ivFootIcon = view.findViewById(R.id.ivFootIcon)
        ivProgressArc = view.findViewById(R.id.ivProgressArc)

        val bounceAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        ivFootIcon.startAnimation(bounceAnimation)
    }

    private fun setupSensor() {
        sensorManager = requireContext().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            tvStatus.text = "Using accelerometer for step detection"
        } else {
            tvStatus.text = "Step counter sensor available"
        }
    }

    private fun setupClickListeners() {
        btnSetGoal.setOnClickListener {
            setDailyGoal()
        }

        btnStart.setOnClickListener {
            toggleStepCounting()
        }

        btnReset.setOnClickListener {
            resetStepCount()
        }
    }

    private fun setDailyGoal() {
        val goalText = etStepGoal.text.toString()
        if (goalText.isNotEmpty()) {
            val newGoal = goalText.toInt()
            if (newGoal > 0) {
                dailyGoal = newGoal
                etStepGoal.text.clear()
                updateUI()
                showToast("Daily goal set to $newGoal steps")
            } else {
                showToast("Please enter a positive number")
            }
        } else {
            showToast("Please enter a step goal")
        }
    }

    private fun toggleStepCounting() {
        if (!isCounting) {
            startStepCounting()
        } else {
            stopStepCounting()
        }
    }

    private fun startStepCounting() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
            isCounting = true
            startTime = System.currentTimeMillis()
            btnStart.text = "STOP"
            tvStatus.text = "Counting steps..."
            startStepAnimation()
            showToast("Step counting started")
        } else {
            showToast("Step sensor not available on this device")
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
        isCounting = false
        activeTime += System.currentTimeMillis() - startTime
        btnStart.text = "START"
        tvStatus.text = "Step counting stopped"
        stopStepAnimation()
        showToast("Step counting stopped")
    }

    private fun resetStepCount() {
        stepCount = 0
        activeTime = 0
        updateUI()
        showToast("Step count reset")
    }

    private fun startStepAnimation() {//https://developer.android.com/develop/ui/views/animations/overview and Threads are from
        stepAnimationRunnable = object : Runnable {
            override fun run() {
                val bounceAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
                ivFootIcon.startAnimation(bounceAnimation)
                handler.postDelayed(this, 2000)
            }
        }
        handler.post(stepAnimationRunnable!!)
    }

    private fun stopStepAnimation() {
        stepAnimationRunnable?.let {
            handler.removeCallbacks(it)
        }
        ivFootIcon.clearAnimation()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val newSteps = it.values[0].toInt()
                    if (stepCount == 0) {
                        stepCount = newSteps
                    } else {
                        val stepsSinceLast = newSteps - stepCount
                        if (stepsSinceLast > 0) {
                            stepCount = newSteps
                            updateUI()
                            animateStepIncrease()
                        }
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    detectStepsFromAccelerometer(it.values[0], it.values[1], it.values[2])
                }
            }
        }
    }

    private var lastAcceleration = 0f
    private var stepThreshold = 12f
    private var lastStepTime = 0L

    private fun detectStepsFromAccelerometer(x: Float, y: Float, z: Float) {
        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = acceleration - lastAcceleration
        lastAcceleration = acceleration

        val currentTime = System.currentTimeMillis()

        if (delta > stepThreshold && (currentTime - lastStepTime) > 300) {
            stepCount++
            lastStepTime = currentTime
            updateUI()
            animateStepIncrease()
        }
    }

    private fun animateStepIncrease() {
        val scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        tvStepCount.startAnimation(scaleAnimation)

        val progress = (stepCount.toFloat() / dailyGoal).coerceAtMost(1.0f)
        ivProgressArc.rotation = progress * 360f
    }

    private fun updateUI() {
        tvStepCount.text = stepCount.toString()

        tvGoalProgress.text = "Goal: $stepCount/$dailyGoal"
        tvGoalText.text = "Daily goal: $dailyGoal steps"

        val distance = calculateDistance(stepCount)
        val calories = calculateCalories(stepCount)
        val activeMinutes = calculateActiveTime()

        tvDistance.text = String.format("%.1f km", distance)
        tvCalories.text = "${calories} cal"
        tvActiveTime.text = "${activeMinutes} min"

        updateStatusMessage()
    }

    private fun calculateDistance(steps: Int): Double {
        val distanceMeters = steps * 0.762
        return distanceMeters / 1000 // Convert to kilometers
    }

    private fun calculateCalories(steps: Int): Int {
        return (steps * 0.04).toInt()
    }

    private fun calculateActiveTime(): Long {
        val totalTime = if (isCounting) {
            activeTime + (System.currentTimeMillis() - startTime)
        } else {
            activeTime
        }
        return totalTime / 60000
    }

    private fun updateStatusMessage() {
        val progress = stepCount.toFloat() / dailyGoal
        when {
            stepCount == 0 -> tvStatus.text = "Let's start walking!"
            progress < 0.25 -> tvStatus.text = "Great start! Keep going!"
            progress < 0.5 -> tvStatus.text = "You're doing amazing!"
            progress < 0.75 -> tvStatus.text = "More than halfway there!"
            progress < 1.0 -> tvStatus.text = "Almost at your goal!"
            else -> tvStatus.text = "Goal achieved! 🎉"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    override fun onPause() {
        super.onPause()
        stopStepCounting()
    }

    override fun onResume() {
        super.onResume()
        if (isCounting) {
            startStepCounting()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        stopStepAnimation()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): FootstepFragment {
            return FootstepFragment()
        }
    }
}