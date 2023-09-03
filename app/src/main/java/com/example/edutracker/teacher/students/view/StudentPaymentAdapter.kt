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
import com.example.edutracker.databinding.AttendanceItemBinding


class StudentPaymentDiffUtil : DiffUtil.ItemCallback<Pair<String, String>>() {

    override fun areItemsTheSame(
        oldItem: Pair< String, String>,
        newItem: Pair< String, String>
    ): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(
        oldItem: Pair< String, String>,
        newItem: Pair< String, String>
    ): Boolean {
        return oldItem == newItem
    }
}

class StudentPaymentAdapter(private var clickListener: (Pair< String, String>) -> Unit) :
    ListAdapter<Pair< String, String>, StudentPaymentAdapter.GroupPaymentViewHolder>(StudentPaymentDiffUtil()) {
    lateinit var context: Context

    inner class GroupPaymentViewHolder(val binding: AttendanceItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupPaymentViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context = parent.context
        val binding = AttendanceItemBinding.inflate(inflater, parent, false)
        return GroupPaymentViewHolder(binding)
    }


    override fun onBindViewHolder(holder: GroupPaymentViewHolder, position: Int) {
        val currentObject = getItem(position)
        holder.binding.studentNameIenttextView.text=currentObject.first
        var attendanceStatus = currentObject.second
        if (currentObject.second == "null"){
            attendanceStatus =context.getString(R.string.payment_not_done)
        }

        holder.binding.attendancetextView.text=attendanceStatus

        val cardColor = when (attendanceStatus) {
            context.getString(R.string.payment_done) -> R.color.green
            else -> R.color.red
        }
        val textColor = when (attendanceStatus) {
            context.getString(R.string.present) -> R.color.black
            else -> R.color.white
        }
        holder.binding.attendancetextView.setTextColor(ContextCompat.getColor(context, textColor))
        holder.binding.studentNameIenttextView.setTextColor(ContextCompat.getColor(context, textColor))

        holder.binding.attendanceCard.setCardBackgroundColor(ContextCompat.getColor(context, cardColor))

        // Set the attendance image based on the attendance status
        val attendanceImage = when (attendanceStatus) {
            context.getString(R.string.payment_done) -> R.drawable.payment_done
            else -> R.drawable.no_payment
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