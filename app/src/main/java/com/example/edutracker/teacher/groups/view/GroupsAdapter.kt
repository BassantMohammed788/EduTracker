package com.example.edutracker.teacher.groups.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.databinding.GroupItemBinding
import com.example.edutracker.dataclasses.Group


class GroupsDiffUtil : DiffUtil.ItemCallback<Group>() {

    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem == newItem
    }
}

class GroupsAdapter( private var deleteListener: (Group) -> Unit) :
    ListAdapter<Group, GroupsAdapter.GroupsViewHolder>(GroupsDiffUtil()) {
    lateinit var context: Context
    var gradeLevel : String? = null

    inner class GroupsViewHolder(val binding: GroupItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = GroupItemBinding.inflate(inflater, parent, false)
        return GroupsViewHolder(binding)
    }


    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.groupTextView.text=currentObject.name
        holder.binding.gradeTextView.text=gradeLevel
        holder.binding.deleteIcon.setOnClickListener {
            deleteListener(currentObject)
        }

    }
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.i("TAG", "getItemCount: $count")
        return count
    }

    @JvmName("setGradeLevel1")
    fun setGradeLevel(gradeLevell:String){
        this.gradeLevel=gradeLevell
    }

}