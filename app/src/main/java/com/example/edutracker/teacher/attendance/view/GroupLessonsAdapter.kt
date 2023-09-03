package com.example.edutracker.teacher.attendance.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.GradeLevelItemBinding
import com.example.edutracker.dataclasses.Lesson


class GroupLessonsAdapter(private var GroupLessonsList: List<Lesson>, private var clickListener: (Lesson) -> Unit) :
    RecyclerView.Adapter<GroupLessonsAdapter.GroupLessonsViewHolder>() {
    private lateinit var binding: GradeLevelItemBinding
    lateinit var context: Context
    inner class GroupLessonsViewHolder(var binding: GradeLevelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupLessonsViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context=parent.context
        binding = GradeLevelItemBinding.inflate(inflater, parent, false)
        return GroupLessonsViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: GroupLessonsViewHolder, position: Int) {
        val currentLevel = GroupLessonsList[position]
        holder.binding.GradeName.text = currentLevel.time
        val hoverBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_hover_background)
        holder.binding.gradeLevelItem.setOnClickListener {
            holder.binding.constraint.background = hoverBackgroundDrawable
            clickListener(currentLevel)
        }

    }

    override fun getItemCount(): Int {
        return GroupLessonsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setGradeLevelsList(currencyList: List<Lesson>) {
        this.GroupLessonsList = currencyList
        notifyDataSetChanged()
    }
}