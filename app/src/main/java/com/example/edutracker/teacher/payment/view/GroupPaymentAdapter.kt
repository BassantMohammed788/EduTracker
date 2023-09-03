package com.example.edutracker.teacher.payment.view

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



class GroupPaymentDiffUtil : DiffUtil.ItemCallback<Triple<String, String, String>>() {

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

class GroupPaymentAdapter(private var clickListener: (Triple<String, String, String>) -> Unit) :
    ListAdapter<Triple<String, String, String>, GroupPaymentAdapter.GroupPaymentViewHolder>(GroupPaymentDiffUtil()) {
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
        holder.binding.studentNameIenttextView.text=currentObject.second
        var attendanceStatus = currentObject.third
        if (currentObject.third == "null"){
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