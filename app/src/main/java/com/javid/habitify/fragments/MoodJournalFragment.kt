package com.javid.habitify.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.javid.habitify.R
import com.javid.habitify.adapters.MoodHistoryAdapter
import com.javid.habitify.adapters.MoodOptionsAdapter
import com.javid.habitify.model.MoodEntry
import com.javid.habitify.model.MoodOption
import com.javid.habitify.utils.PrefsManager

class MoodJournalFragment : Fragment(), MoodOptionsAdapter.OnMoodClickListener {

    private lateinit var prefsManager: PrefsManager
    private val gson = Gson()
    private val moodEntries = mutableListOf<MoodEntry>()
    private var selectedMood: MoodOption? = null

    private val moodOptions = listOf(
        MoodOption("😊", "Happy", "Feeling great and positive", R.color.mood_happy),
        MoodOption("😢", "Sad", "Feeling down or upset", R.color.mood_sad),
        MoodOption("😡", "Angry", "Feeling frustrated or mad", R.color.mood_angry),
        MoodOption("😴", "Tired", "Feeling exhausted or sleepy", R.color.mood_tired),
        MoodOption("😌", "Calm", "Feeling peaceful and relaxed", R.color.mood_calm),
        MoodOption("😰", "Anxious", "Feeling worried or nervous", R.color.mood_anxious),
        MoodOption("🤩", "Excited", "Feeling enthusiastic and eager", R.color.mood_excited),
        MoodOption("😔", "Disappointed", "Feeling let down", R.color.mood_disappointed),
        MoodOption("😎", "Confident", "Feeling self-assured", R.color.mood_confident),
        MoodOption("🤒", "Sick", "Feeling unwell or ill", R.color.mood_sick),
        MoodOption("😍", "Loved", "Feeling loved and appreciated", R.color.mood_loved),
        MoodOption("🤔", "Thoughtful", "Deep in thought", R.color.mood_thoughtful)
    )

    private lateinit var moodOptionsAdapter: MoodOptionsAdapter
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_mood_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = PrefsManager(requireContext())
        setupViews(view)
        loadMoodEntries()
    }

    private fun setupViews(view: View) {
        val moodRecyclerView: RecyclerView = view.findViewById(R.id.moodRecyclerView)
        val etMoodNote: EditText = view.findViewById(R.id.etMoodNote)
        val btnSaveMood: Button = view.findViewById(R.id.btnSaveMood)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val moodHistoryRecyclerView: RecyclerView = view.findViewById(R.id.moodHistoryRecyclerView)
        val tvSelectedMood: TextView = view.findViewById(R.id.tvSelectedMood)

        moodOptionsAdapter = MoodOptionsAdapter(moodOptions, this)
        moodRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = moodOptionsAdapter
        }

        moodHistoryAdapter = MoodHistoryAdapter(moodEntries)
        moodHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodHistoryAdapter
        }

        btnSaveMood.setOnClickListener {
            if (selectedMood != null) {
                val note = etMoodNote.text.toString().trim()

                val moodEntry = MoodEntry(
                    mood = selectedMood!!.name,
                    moodEmoji = selectedMood!!.emoji,
                    note = note
                )

                saveMoodEntry(moodEntry)
                showToast("Mood saved: ${selectedMood!!.emoji} ${selectedMood!!.name}")
            } else {
                showToast("Please select a mood first")
            }
        }

        btnCancel.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onMoodClick(moodOption: MoodOption) {
        selectedMood = moodOption
        val tvSelectedMood: TextView = requireView().findViewById(R.id.tvSelectedMood)
        tvSelectedMood.text = "Selected: ${moodOption.emoji} ${moodOption.name}"

        val etMoodNote: EditText = requireView().findViewById(R.id.etMoodNote)
        etMoodNote.hint = "How are you feeling? (${moodOption.description})"
    }

    private fun saveMoodEntry(moodEntry: MoodEntry) {
        moodEntries.add(0, moodEntry)

        val allMoodsJson = prefsManager.getUserPreference("mood_entries", "")
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        val allMoods = if (allMoodsJson.isNotEmpty()) {
            gson.fromJson<MutableList<MoodEntry>>(allMoodsJson, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        allMoods.add(0, moodEntry)
        val updatedMoodsJson = gson.toJson(allMoods)
        prefsManager.setUserPreference("mood_entries", updatedMoodsJson)

        moodHistoryAdapter.updateData(moodEntries)
    }

    private fun loadMoodEntries() {
        val allMoodsJson = prefsManager.getUserPreference("mood_entries", "")
        if (allMoodsJson.isNotEmpty()) {
            val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
            val loadedMoods = gson.fromJson<MutableList<MoodEntry>>(allMoodsJson, type)
            moodEntries.clear()
            loadedMoods?.let {
                moodEntries.addAll(it.take(5))
            }
            moodHistoryAdapter.updateData(moodEntries)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): MoodJournalFragment {
            return MoodJournalFragment()
        }
    }
}
