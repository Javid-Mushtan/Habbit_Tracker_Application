package com.javid.habitify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.javid.habitify.R
import com.javid.habitify.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(private var moodEntries: List<MoodEntry>) :
    RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder>() {

    inner class MoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMoodEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        private val tvMoodName: TextView = itemView.findViewById(R.id.tvMoodName)
        private val tvMoodNote: TextView = itemView.findViewById(R.id.tvMoodNote)
        private val tvMoodTime: TextView = itemView.findViewById(R.id.tvMoodTime)

        fun bind(moodEntry: MoodEntry) {
            tvMoodEmoji.text = moodEntry.moodEmoji
            tvMoodName.text = moodEntry.mood
            tvMoodNote.text = if (moodEntry.note.isNotEmpty()) moodEntry.note else "No note"

            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            tvMoodTime.text = dateFormat.format(Date(moodEntry.timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodHistoryViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateData(newEntries: List<MoodEntry>) {
        this.moodEntries = newEntries
        notifyDataSetChanged()
    }
}