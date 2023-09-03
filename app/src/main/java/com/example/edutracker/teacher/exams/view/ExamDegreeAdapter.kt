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
import com.example.edutracker.databinding.ExamDegreeItemBinding
import com.example.edutracker.dataclasses.ExamDegree


class ExamDegreeDiffUtil : DiffUtil.ItemCallback<Triple<String, String, ExamDegree>>() {

    override fun areItemsTheSame(
        oldItem: Triple<String, String, ExamDegree>,
        newItem: Triple<String, String, ExamDegree>
    ): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(
        oldItem: Triple<String, String, ExamDegree>,
        newItem: Triple<String, String, ExamDegree>
    ): Boolean {
        return oldItem == newItem
    }
}

class ExamDegreeAdapter(private var clickListener: (Triple<String, String, ExamDegree>) -> Unit) :
    ListAdapter<Triple<String, String, ExamDegree>, ExamDegreeAdapter.ExamDegreeViewHolder>(ExamDegreeDiffUtil()) {
    lateinit var context: Context

    inner class ExamDegreeViewHolder(val binding: ExamDegreeItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamDegreeViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = ExamDegreeItemBinding.inflate(inflater, parent, false)
        return ExamDegreeViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ExamDegreeViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.studentNameTextView.text=currentObject.second
        var examDegree = currentObject.third.examDegree
        if (currentObject.third.examDegree == "null"){
            examDegree =context.getString(R.string.no_degree)
        }

        holder.binding.examDegreetextView.text=examDegree

        val cardColor = when (examDegree) {
            context.getString(R.string.no_degree) -> R.color.white
            else -> R.color.green
        }
        val textColor = when (examDegree) {
            context.getString(R.string.no_degree) -> R.color.black
            else -> R.color.white
        }

        holder.binding.attendanceCard.setCardBackgroundColor(ContextCompat.getColor(context, cardColor))


        holder.binding.attendanceCard.setOnClickListener {
            clickListener(currentObject)
        }


    }
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.i("TAG", "getItemCount: $count")
        return count
    }


}