package com.javid.habitify.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.javid.habitify.R

class SpecialHabitsFragment : Fragment() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var mainMenuLayout: View
    private lateinit var fragmentContainer: View
    private lateinit var cardBmi: View
    private lateinit var cardWater: View
    private lateinit var cardSteps: View
    private lateinit var cardHeart: View

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_special_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupToolbar()
    }

    private fun initializeViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        mainMenuLayout = view.findViewById(R.id.main_menu_layout)
        fragmentContainer = view.findViewById(R.id.fragment_container)
        cardBmi = view.findViewById(R.id.card_bmi)
        cardWater = view.findViewById(R.id.card_water)
        cardSteps = view.findViewById(R.id.card_steps)
        cardHeart = view.findViewById(R.id.card_heart)

    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                // Handle back navigation in fragment context
                activity?.onBackPressed()
            }
        }
    }

    private fun setupClickListeners() {
        cardBmi.setOnClickListener {
            showFragment(com.javid.habitify.fragments.BmiCalculatorFragment(), "BMI Calculator")
        }

        cardWater.setOnClickListener {
            showFragment(com.javid.habitify.fragments.WaterTrackerFragment.newInstance(), "Water Drinking")
        }

        cardSteps.setOnClickListener {
            showFragment(com.javid.habitify.fragments.FootstepFragment.newInstance(), "Footstep Counting")
        }

        cardHeart.setOnClickListener {
            showFragment(com.javid.habitify.fragments.HeartRateFragment.newInstance(), "Heart Rate")
        }
    }

    private fun showFragment(fragment: Fragment, title: String) {
        mainMenuLayout.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        toolbar.title = title

        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance(): SpecialHabitsFragment {
            return SpecialHabitsFragment()
        }
    }
}
