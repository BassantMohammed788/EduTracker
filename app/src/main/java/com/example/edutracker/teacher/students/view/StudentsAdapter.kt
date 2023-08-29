package com.example.edutracker.teacher.students.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.databinding.AssistantItemBinding
import com.example.edutracker.dataclasses.Student


class StudentDiffUtil : DiffUtil.ItemCallback<Student>() {

    override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
        return oldItem.email == newItem.email
    }

    override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
        return oldItem == newItem
    }
}

class StudentsAdapter( private var clickListener: (Student) -> Unit) :
    ListAdapter<Student, StudentsAdapter.StudentViewHolder>(StudentDiffUtil()) {
    lateinit var context: Context

    inner class StudentViewHolder(val binding: AssistantItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = AssistantItemBinding.inflate(inflater, parent, false)
        return StudentViewHolder(binding)
    }


    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.textAssistant.text=currentObject.name
        holder.binding.AssistantCard.setOnClickListener {
            clickListener(currentObject)
        }


    }
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.i("TAG", "getItemCount: $count")
        return count
    }


}