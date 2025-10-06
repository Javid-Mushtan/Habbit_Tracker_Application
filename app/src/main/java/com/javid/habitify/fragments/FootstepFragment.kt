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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.javid.habitify.R
import com.javid.habitify.model.FootstepData
import com.javid.habitify.viewmodels.FootstepViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FootstepFragment : Fragment(), SensorEventListener {

    private val viewModel: FootstepViewModel by viewModels()

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

    private val handler = Handler(Looper.getMainLooper())
    private var stepAnimationRunnable: Runnable? = null

    private var lastAcceleration = 0f
    private var stepThreshold = 12f
    private var lastStepTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_footsteps, container, false)

        initializeViews(view)
        setupSensor()
        setupClickListeners()
        setupObservers()

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
            viewModel.resetData()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.footstepData.collect { data ->
                updateUI(data)
            }
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showToast(message)
            }
        }
    }

    private fun setDailyGoal() {
        val goalText = etStepGoal.text.toString()
        if (goalText.isNotEmpty()) {
            val newGoal = goalText.toInt()
            if (newGoal > 0) {
                viewModel.setDailyGoal(newGoal)
                etStepGoal.text.clear()
            } else {
                showToast("Please enter a positive number")
            }
        } else {
            showToast("Please enter a step goal")
        }
    }

    private fun toggleStepCounting() {
        if (!viewModel.footstepData.value.isCounting) {
            startStepCounting()
        } else {
            stopStepCounting()
        }
    }

    private fun startStepCounting() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
            viewModel.startCounting()
            btnStart.text = "STOP"
            startStepAnimation()
        } else {
            showToast("Step sensor not available on this device")
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
        viewModel.stopCounting()
        btnStart.text = "START"
        stopStepAnimation()
    }

    private fun startStepAnimation() {
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
                    viewModel.updateStepCount(newSteps, fromSensor = true)
                    animateStepIncrease()
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    detectStepsFromAccelerometer(it.values[0], it.values[1], it.values[2])
                }
            }
        }
    }

    private fun detectStepsFromAccelerometer(x: Float, y: Float, z: Float) {
        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = acceleration - lastAcceleration
        lastAcceleration = acceleration

        val currentTime = System.currentTimeMillis()

        if (delta > stepThreshold && (currentTime - lastStepTime) > 300) {
            viewModel.incrementStep()
            lastStepTime = currentTime
            animateStepIncrease()
        }
    }

    private fun animateStepIncrease() {
        val scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        tvStepCount.startAnimation(scaleAnimation)

        val progress = viewModel.goalProgress.coerceAtMost(1.0f)
        ivProgressArc.rotation = progress * 360f
    }

    private fun updateUI(data: FootstepData) {
        tvStepCount.text = data.stepCount.toString()
        tvGoalProgress.text = "Goal: ${data.stepCount}/${data.dailyGoal}"
        tvGoalText.text = "Daily goal: ${data.dailyGoal} steps"

        tvDistance.text = String.format("%.1f km", viewModel.distance)
        tvCalories.text = "${viewModel.calories} cal"
        tvActiveTime.text = "${viewModel.activeMinutes} min"

        tvStatus.text = viewModel.getStatusMessage()

        val progress = viewModel.goalProgress.coerceAtMost(1.0f)
        ivProgressArc.rotation = progress * 360f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // if you want mj mushtan
    }

    override fun onPause() {
        super.onPause()
        stopStepCounting()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.footstepData.value.isCounting) {
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