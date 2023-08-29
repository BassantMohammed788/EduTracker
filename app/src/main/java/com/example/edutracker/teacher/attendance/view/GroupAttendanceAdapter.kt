package com.example.edutracker.teacher.attendance.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.AttendanceItemBinding

class GroupAttendanceDiffUtil : DiffUtil.ItemCallback<Triple<String, String, String>>() {

    override fun areItemsTheSame(
        oldItem: Triple<String, String, String>,
        newItem: Triple<String, String, String>
    ): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(
        oldItem: Triple<String, String, String>,
        newItem: Triple<String, String, String>
    ): Boolean {
        return oldItem == newItem
    }
}

class GroupAttendanceAdapter(private var clickListener: (Triple<String, String, String>) -> Unit) :
    ListAdapter<Triple<String, String, String>, GroupAttendanceAdapter.GroupAttendanceViewHolder>(GroupAttendanceDiffUtil()) {
    lateinit var context: Context

    inner class GroupAttendanceViewHolder(val binding: AttendanceItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupAttendanceViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = AttendanceItemBinding.inflate(inflater, parent, false)
        return GroupAttendanceViewHolder(binding)
    }


    override fun onBindViewHolder(holder: GroupAttendanceViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.studentNameIenttextView.text=currentObject.second
        holder.binding.attendancetextView.text=currentObject.third
        val attendanceStatus = currentObject.third
        val cardColor = when (attendanceStatus) {
            context.getString(R.string.present) -> R.color.green
            context.getString(R.string.absent)  -> R.color.red
            context.getString(R.string.late) -> R.color.orange
            context.getString(R.string.permitted)  -> R.color.primary
            else -> R.color.white
        }
        val textColor = when (attendanceStatus) {
            context.getString(R.string.present) -> R.color.white
            context.getString(R.string.absent)  -> R.color.white
            context.getString(R.string.late) -> R.color.white
            context.getString(R.string.permitted)  -> R.color.white
            else -> R.color.black
        }
        holder.binding.attendancetextView.setTextColor(ContextCompat.getColor(context, textColor))
        holder.binding.studentNameIenttextView.setTextColor(ContextCompat.getColor(context, textColor))

        holder.binding.attendanceCard.setCardBackgroundColor(ContextCompat.getColor(context, cardColor))

        // Set the attendance image based on the attendance status
        val attendanceImage = when (attendanceStatus) {
            context.getString(R.string.present) -> R.drawable.attendee
            context.getString(R.string.absent)  ->R.drawable.absent
            context.getString(R.string.late) -> R.drawable.late
            context.getString(R.string.permitted)  -> R.drawable.permission
            else -> R.drawable.no_attendance_yet
        }
        holder.binding.attendanceImageView.setImageResource(attendanceImage)


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