package com.example.edutracker.teacher.lessons.view


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
import com.example.edutracker.databinding.GroupItemBinding
import com.example.edutracker.dataclasses.Group


class GroupDiffUtil : DiffUtil.ItemCallback<Group>() {

    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem == newItem
    }
}

class GroupAdapter( private var Listener: (Group) -> Unit) :
    ListAdapter<Group, GroupAdapter.GroupsViewHolder>(GroupDiffUtil()) {
    lateinit var context: Context

    inner class GroupsViewHolder(val binding: GradeLevelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = GradeLevelItemBinding.inflate(inflater, parent, false)
        return GroupsViewHolder(binding)
    }


    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.GradeName.text=currentObject.name
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