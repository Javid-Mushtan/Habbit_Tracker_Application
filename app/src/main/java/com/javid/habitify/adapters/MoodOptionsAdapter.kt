package com.javid.habitify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.javid.habitify.R
import com.javid.habitify.model.MoodOption

class MoodOptionsAdapter(
    private val moodOptions: List<MoodOption>,
    private val listener : OnMoodClickListener
) : RecyclerView.Adapter<MoodOptionsAdapter.MoodViewHolder>() {

    private var selectedPosition = -1

    interface OnMoodClickListener {
        fun onMoodClick(moodOption: MoodOption)
    }

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMoodEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        private val tvMoodName: TextView = itemView.findViewById(R.id.tvMoodName)

        fun bind(moodOption: MoodOption, position: Int){
            tvMoodEmoji.text = moodOption.emoji
            tvMoodName.text = moodOption.name

            if(position == selectedPosition){
                itemView.setBackgroundResource(R.drawable.mood_option_background)
                tvMoodName.setTextColor(getColor(itemView.context, android.R.color.white))
            } else {
                itemView.setBackgroundResource(R.drawable.mood_option_background)
                tvMoodName.setTextColor(getColor(itemView.context, R.color.primary_text))
            }

            val backgroundColor = if (position == selectedPosition) {
                moodOption.colorRes
            } else {
                R.color.mood_default
            }
            itemView.setBackgroundColor(getColor(itemView.context,backgroundColor))

            itemView.setOnClickListener {
                selectedPosition = position
                listener.onMoodClick(moodOption)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_option,parent,false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MoodViewHolder,
        position: Int
    ) {
        holder.bind( moodOptions[position], position)
    }

    override fun getItemCount(): Int {
        return moodOptions.size
    }

}