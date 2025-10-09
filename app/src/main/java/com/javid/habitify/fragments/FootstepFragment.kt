package com.javid.habitify.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private var stepSensor: Sensor? = null

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
    private var initialStepCount = 0
    private var isStepCounterSensor = false
    private var hasStepCounterPermission = false

    companion object {
        private const val ACTIVITY_RECOGNITION_PERMISSION_REQUEST = 1001

        fun newInstance(): FootstepFragment {
            return FootstepFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_footsteps, container, false)

        initializeViews(view)
        checkPermissions()
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

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasStepCounterPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasStepCounterPermission) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_PERMISSION_REQUEST
                )
            }
        } else {
            hasStepCounterPermission = true
        }
    }

    private fun setupSensor() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            isStepCounterSensor = true
            tvStatus.text = "Step counter sensor available"
        } else {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (stepSensor != null) {
                isStepCounterSensor = false
                tvStatus.text = "Using accelerometer for step detection"
            } else {
                tvStatus.text = "No step sensor available"
                btnStart.isEnabled = false
            }
        }
    }

    private fun setupClickListeners() {
        btnSetGoal.setOnClickListener {
            setDailyGoal()
        }

        btnStart.setOnClickListener {
            if (!hasStepCounterPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                showToast("Please grant activity recognition permission first")
                checkPermissions()
                return@setOnClickListener
            }
            toggleStepCounting()
        }

        btnReset.setOnClickListener {
            viewModel.resetData()
            initialStepCount = 0
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
                    val currentSteps = it.values[0].toInt()

                    if (initialStepCount == 0) {
                        initialStepCount = currentSteps
                    }

                    val stepsSinceStart = currentSteps - initialStepCount
                    if (stepsSinceStart >= 0) {
                        viewModel.updateStepCount(stepsSinceStart, fromSensor = true)
                        animateStepIncrease()
                    }
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ACTIVITY_RECOGNITION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasStepCounterPermission = true
                    showToast("Permission granted! You can now start step counting.")
                } else {
                    showToast("Permission denied. Step counting may not work properly.")
                }
            }
        }
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
        handler.removeCallbacksAndMessages(null)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}