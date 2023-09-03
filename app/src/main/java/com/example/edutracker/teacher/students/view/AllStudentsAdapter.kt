package com.example.edutracker.teacher.students.view

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
import com.example.edutracker.dataclasses.Student

class AllStudentDiffUtil : DiffUtil.ItemCallback<Student>() {

    override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
        return oldItem.email == newItem.email
    }

    override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
        return oldItem == newItem
    }
}

class AllStudentsAdapter( private var clickListener: (Student) -> Unit) :
    ListAdapter<Student, AllStudentsAdapter.AllStudentViewHolder>(AllStudentDiffUtil()) {
    lateinit var context: Context

    inner class AllStudentViewHolder(val binding: GradeLevelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllStudentViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = GradeLevelItemBinding.inflate(inflater, parent, false)
        return AllStudentViewHolder(binding)
    }


    override fun onBindViewHolder(holder: AllStudentViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.GradeName.text = currentObject.name
        val hoverBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_hover_background)
        holder.binding.gradeLevelItem.setOnClickListener {
            holder.binding.constraint.background = hoverBackgroundDrawable
            clickListener(currentObject)
        }
    }
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.i("TAG", "getItemCount: $count")
        return count
    }


}