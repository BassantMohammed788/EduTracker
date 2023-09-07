package com.example.edutracker.teacher.students.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentStudentPaymentBinding
import com.example.edutracker.databinding.PaymentDialogBinding
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class StudentPaymentFragment : Fragment() {
    lateinit var binding: FragmentStudentPaymentBinding
    private val args: StudentPaymentFragmentArgs by navArgs()
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var studentPaymentAdapter:StudentPaymentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var student: Student
    private var semesterVar: String? = null
    private var paymentList = mutableListOf<Pair<String,String>>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        student=args.student
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()

        studentPaymentAdapter=StudentPaymentAdapter(paymentClickLambda)
        recyclerView = binding.paymentRecycler
        recyclerView.apply {
            adapter = studentPaymentAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        getStudentPaymentStatusForAllMonths()

        binding.studentName.text=student.name

    }



    private fun getStudentPaymentStatusForAllMonths(){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getStudentPaymentStatusForAllMonths(student.email,semesterVar!!)
                studentsViewModel.getStudentPaymentStatus.collect{ result->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.paymentProgressBar.visibility=View.VISIBLE
                            binding.paymentRecycler.visibility=View.INVISIBLE
                            binding.noMonthsTv.visibility=View.INVISIBLE
                            binding.noDataAnimationView.visibility=View.INVISIBLE

                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()){
                                binding.paymentProgressBar.visibility=View.INVISIBLE
                                binding.paymentRecycler.visibility=View.INVISIBLE
                                binding.noMonthsTv.visibility=View.VISIBLE
                                binding.noDataAnimationView.visibility=View.VISIBLE

                            }else{
                                binding.paymentProgressBar.visibility=View.INVISIBLE
                                binding.paymentRecycler.visibility=View.VISIBLE
                                binding.noMonthsTv.visibility=View.INVISIBLE
                                binding.noDataAnimationView.visibility=View.INVISIBLE

                                studentPaymentAdapter.submitList(result.data)
                                paymentList=result.data.toMutableList()

                            }
                        }

                        is FirebaseState.Failure -> {

                            Log.i("TAG", "onViewCreated: fail")
                        }
                    }
                }
            }
        }else{
            Toast.makeText(requireContext(),getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

        }
    }
    private val paymentClickLambda = { payment: Pair< String, String> ->
        val builder = AlertDialog.Builder(requireContext())
        val paymentDialog = PaymentDialogBinding.inflate(layoutInflater)
        builder.setView(paymentDialog.root)
        val dialog = builder.create()
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        paymentDialog.studentNameTV.visibility=View.GONE

        paymentDialog.paymentDoneCard.setOnClickListener {
            addStudentAttendance(getString(R.string.payment_done),payment)
            dialog.dismiss()
        }

        paymentDialog.paymentNotDoneCard.setOnClickListener {
            addStudentAttendance(getString(R.string.payment_not_done),payment)
            dialog.dismiss()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun  replaceItem(list: MutableList<Pair<String,String>>, oldItem: Pair<String,String>, newItem: Pair<String,String>) {
        val index = list.indexOf(oldItem)
        if (index != -1) {
            list[index] = newItem
        }
        studentPaymentAdapter.notifyDataSetChanged()

    }
    private fun addStudentAttendance(paymentStatus:String, payment:Pair<String,String>){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.addStudentPaymentForMonth(semesterVar!!,student.activeGradeLevel,payment.first,student.email,paymentStatus)
                replaceItem(paymentList,payment,Pair(payment.first,paymentStatus))
                studentPaymentAdapter.submitList(paymentList)
                Log.i("TAG", ": $paymentList")
            }}
        else{
            Toast.makeText(requireContext(),getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }
}