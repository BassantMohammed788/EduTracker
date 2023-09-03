package com.example.edutracker.teacher.exams.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.GradeLevelItemBinding
import com.example.edutracker.databinding.LessonItemBinding
import com.example.edutracker.dataclasses.Exam


class ExamDiffUtil : DiffUtil.ItemCallback<Exam>() {

    override fun areItemsTheSame(oldItem: Exam, newItem: Exam): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Exam, newItem: Exam): Boolean {
        return oldItem == newItem
    }
}

class ExamsAdapter(private var Listener: (Exam) -> Unit) :
    ListAdapter<Exam, ExamsAdapter.ExamsViewHolder>(ExamDiffUtil()) {
    lateinit var context: Context

    inner class ExamsViewHolder(val binding: GradeLevelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamsViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = GradeLevelItemBinding.inflate(inflater, parent, false)
        return ExamsViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ExamsViewHolder, position: Int) {
        val currentObject = getItem(position)

        holder.binding.GradeName.text=currentObject.examName
        val hoverBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_hover_background)
        holder.binding.gradeLevelItem.setOnClickListener {
            holder.binding.constraint.background = hoverBackgroundDrawable
            Listener(currentObject)
        }
    }
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.i("TAG", "getItemCount: $count")
        return count
    }

}