package com.javid.habitify.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.javid.habitify.R

class BmiCalculatorFragment : Fragment() {

    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnCalculate: Button
    private lateinit var cardResult: CardView
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bmi_calculator, container, false)

        // Initialize views
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        btnCalculate = view.findViewById(R.id.btnCalculate)
        cardResult = view.findViewById(R.id.cardResult)
        tvBmiValue = view.findViewById(R.id.tvBmiValue)
        tvBmiCategory = view.findViewById(R.id.tvBmiCategory)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnCalculate.setOnClickListener {
            calculateBMI()
        }
    }

    private fun calculateBMI() {
        val weightText = etWeight.text.toString()
        val heightText = etHeight.text.toString()

        if (weightText.isBlank() || heightText.isBlank()) {
            showSnackbar("Please enter both weight and height")
            return
        }

        try {
            val weight = weightText.toFloat()
            val height = heightText.toFloat() / 100

            if (weight <= 0 || height <= 0) {
                showSnackbar("Weight and height must be positive numbers")
                return
            }

            val bmi = weight / (height * height)
            displayBMIResult(bmi)

        } catch (e: NumberFormatException) {
            showSnackbar("Please enter valid numbers")
        }
    }

    private fun displayBMIResult(bmi: Float) {
        cardResult.visibility = View.VISIBLE

        val formattedBmi = "%.1f".format(bmi)
        tvBmiValue.text = formattedBmi

        val (category, colorRes) = getBmiCategory(bmi)
        tvBmiCategory.text = category
        tvBmiValue.setTextColor(requireContext().getColor(colorRes))
    }

    private fun getBmiCategory(bmi: Float): Pair<String, Int> {
        return when {
            bmi < 18.5 -> Pair("Underweight", R.color.bmi_underweight)
            bmi < 25 -> Pair("Normal weight", R.color.bmi_normal)
            bmi < 30 -> Pair("Overweight", R.color.bmi_overweight)
            else -> Pair("Obese", R.color.bmi_obese)
        }
    }

    private fun showSnackbar(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): BmiCalculatorFragment {
            return BmiCalculatorFragment()
        }
    }
}