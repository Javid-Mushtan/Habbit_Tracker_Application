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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.javid.habitify.R
import com.javid.habitify.model.HeartRateData
import com.javid.habitify.viewmodel.HeartRateViewModel
import com.javid.habitify.model.HeartRateStatus
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HeartRateFragment : Fragment(), SensorEventListener {

    private val viewModel: HeartRateViewModel by viewModels()

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

    // Views
    private lateinit var tvHeartRate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvZone: TextView
    private lateinit var tvAverage: TextView
    private lateinit var tvMin: TextView
    private lateinit var tvMax: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var ivHeartIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var containerPulse: LinearLayout
    private lateinit var cardHeartRate: CardView

    private val handler = Handler(Looper.getMainLooper())
    private var pulseAnimationRunnable: Runnable? = null
    private var simulatedMeasurementRunnable: Runnable? = null

    private var simulationIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_heart_rate, container, false)
        initializeViews(view)
        setupSensor()
        setupClickListeners()
        setupObservers()
        return view
    }

    private fun initializeViews(view: View) {
        tvHeartRate = view.findViewById(R.id.tvHeartRate)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvZone = view.findViewById(R.id.tvZone)
        tvAverage = view.findViewById(R.id.tvAverage)
        tvMin = view.findViewById(R.id.tvMin)
        tvMax = view.findViewById(R.id.tvMax)
        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)
        btnSave = view.findViewById(R.id.btnSave)
        btnReset = view.findViewById(R.id.btnReset)
        ivHeartIcon = view.findViewById(R.id.ivHeartIcon)
        progressBar = view.findViewById(R.id.progressBar)
        containerPulse = view.findViewById(R.id.containerPulse)
        cardHeartRate = view.findViewById(R.id.cardHeartRate)

        // Initial button states
        updateButtonStates(HeartRateStatus.READY)
    }

    private fun setupSensor() {
        sensorManager = requireContext().getSystemService(SensorManager::class.java)

        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (heartRateSensor == null) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        }

        if (heartRateSensor != null) {
            tvStatus.text = "Heart rate sensor available"
        } else {
            tvStatus.text = "Using simulated heart rate data"
        }
    }

    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            startHeartRateMeasurement()
        }

        btnStop.setOnClickListener {
            stopHeartRateMeasurement()
        }

        btnSave.setOnClickListener {
            viewModel.saveMeasurement()
        }

        btnReset.setOnClickListener {
            viewModel.resetMeasurement()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.heartRateData.collect { data ->
                updateUI(data)
            }
        }

        viewModel.heartRateStatus.observe(viewLifecycleOwner) { status ->
            updateButtonStates(status)
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showToast(message)
            }
        }
    }

    private fun startHeartRateMeasurement() {
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            viewModel.startMeasurement()
            startPulseAnimation()
        } else {
            viewModel.startMeasurement()
            startPulseAnimation()
            //startSimulatedMeasurement()
        }
    }

    private fun stopHeartRateMeasurement() {
        sensorManager.unregisterListener(this)
        viewModel.stopMeasurement()
        stopPulseAnimation()
        stopSimulatedMeasurement()
    }

    private fun startPulseAnimation() {
        pulseAnimationRunnable = object : Runnable {
            override fun run() {
                val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
                ivHeartIcon.startAnimation(pulseAnimation)
                containerPulse.startAnimation(pulseAnimation)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(pulseAnimationRunnable!!)
    }

    private fun stopPulseAnimation() {
        pulseAnimationRunnable?.let {
            handler.removeCallbacks(it)
        }
        ivHeartIcon.clearAnimation()
        containerPulse.clearAnimation()
    }

//    private fun startSimulatedMeasurement() {
//        simulationIndex = 0
//        simulatedMeasurementRunnable = object : Runnable {
//            override fun run() {
//                if (simulationIndex < simulatedHeartRates.size) {
//                    val simulatedBPM = simulatedHeartRates[simulationIndex]
//                    viewModel.updateHeartRate(simulatedBPM)
//                    simulationIndex++
//                    handler.postDelayed(this, 2000) // Update every 2 seconds
//                } else {
//                    viewModel.stopMeasurement()
//                }
//            }
//        }
//        handler.post(simulatedMeasurementRunnable!!)
//    }

    private fun stopSimulatedMeasurement() {
        simulatedMeasurementRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun updateUI(data: HeartRateData) {
        tvHeartRate.text = if (data.currentBPM > 0) "${data.currentBPM}" else "--"
        tvStatus.text = data.measurementStatus

        if (data.currentBPM > 0) {
            val zone = viewModel.getHeartRateZone(data.currentBPM)
            tvZone.text = "Zone: $zone"

            val colorRes = viewModel.getStatusColor(data.currentBPM)
            val color = ContextCompat.getColor(requireContext(), colorRes)
            tvHeartRate.setTextColor(color)
            tvZone.setTextColor(color)
        }

        tvAverage.text = "Avg: ${data.averageBPM} BPM"
        tvMin.text = "Min: ${data.minBPM} BPM"
        tvMax.text = "Max: ${data.maxBPM} BPM"

        progressBar.visibility = if (data.isMeasuring) View.VISIBLE else View.GONE
    }

    private fun updateButtonStates(status: HeartRateStatus) {
        when (status) {
            HeartRateStatus.READY -> {
                btnStart.isEnabled = true
                btnStop.isEnabled = false
                btnSave.isEnabled = false
                btnReset.isEnabled = true
            }
            HeartRateStatus.MEASURING -> {
                btnStart.isEnabled = false
                btnStop.isEnabled = true
                btnSave.isEnabled = false
                btnReset.isEnabled = false
            }
            HeartRateStatus.FINISHED -> {
                btnStart.isEnabled = true
                btnStop.isEnabled = false
                btnSave.isEnabled = true
                btnReset.isEnabled = true
            }
            HeartRateStatus.ERROR -> {
                btnStart.isEnabled = true
                btnStop.isEnabled = false
                btnSave.isEnabled = false
                btnReset.isEnabled = true
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_HEART_RATE || it.sensor.type == Sensor.TYPE_HEART_RATE) {
                val heartRate = it.values[0].toInt()
                if (heartRate > 0) {
                    viewModel.updateHeartRate(heartRate)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                viewModel.setError("Heart rate measurement unreliable")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                tvStatus.text = "Low accuracy - keep finger steady"
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                tvStatus.text = "Medium accuracy - almost there"
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                tvStatus.text = "High accuracy - good reading"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopHeartRateMeasurement()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        stopPulseAnimation()
        stopSimulatedMeasurement()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): HeartRateFragment {
            return HeartRateFragment()
        }
    }
}