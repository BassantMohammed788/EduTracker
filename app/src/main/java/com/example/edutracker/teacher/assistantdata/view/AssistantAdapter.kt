package com.example.edutracker.teacher.assistantdata.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.databinding.AssistantItemBinding
import com.example.edutracker.dataclasses.Assistant

class AssistantDiffUtil : DiffUtil.ItemCallback<Assistant>() {

    override fun areItemsTheSame(oldItem: Assistant, newItem: Assistant): Boolean {
        return oldItem.email == newItem.email
    }

    override fun areContentsTheSame(oldItem: Assistant, newItem: Assistant): Boolean {
        return oldItem == newItem
    }
}

class AssistantAdapter( private var clickListener: (Assistant) -> Unit) :
    ListAdapter<Assistant, AssistantAdapter.AssistantViewHolder>(AssistantDiffUtil()) {
    lateinit var context: Context

    inner class AssistantViewHolder(val binding: AssistantItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssistantViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = AssistantItemBinding.inflate(inflater, parent, false)
        return AssistantViewHolder(binding)
    }


    override fun onBindViewHolder(holder: AssistantViewHolder, position: Int) {
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