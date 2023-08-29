package com.example.edutracker.teacher.lessons.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.databinding.LessonItemBinding
import com.example.edutracker.dataclasses.Lesson


class LessonDiffUtil : DiffUtil.ItemCallback<Lesson>() {

    override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        return oldItem == newItem
    }
}

class LessonAdapter( private var Listener: (Lesson) -> Unit) :
    ListAdapter<Lesson, LessonAdapter.GroupsViewHolder>(LessonDiffUtil()) {
    lateinit var context: Context

    inner class GroupsViewHolder(val binding: LessonItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = LessonItemBinding.inflate(inflater, parent, false)
        return GroupsViewHolder(binding)
    }


    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val currentObject = getItem(position)
        val parts = currentObject.time.split(", ")
        holder.binding.dayTextView.text=parts[0]
        holder.binding.dateTextView.text=parts[1]
        holder.binding.timeTextView.text=parts[2]
        holder.binding.deleteIcon.setOnClickListener {
            Listener(currentObject)
        }

    }
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.i("TAG", "getItemCount: $count")
        return count
    }

}