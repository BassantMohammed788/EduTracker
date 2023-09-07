package com.example.edutracker.teacher.payment.view

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
import com.example.edutracker.databinding.FragmentRecordPaymentBinding
import com.example.edutracker.databinding.PaymentDialogBinding
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.payment.viewmodel.PaymentViewModel
import com.example.edutracker.teacher.payment.viewmodel.PaymentViewModelFactory
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class RecordPaymentFragment : Fragment() {

    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var paymentViewModelFactory: PaymentViewModelFactory
    private lateinit var groupPaymentAdapter: GroupPaymentAdapter
    lateinit var recyclerView: RecyclerView
    private val args: RecordPaymentFragmentArgs by navArgs()
    private var gradeVar: String? = null
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var monthVar: String? = null
    private var teacherIdVar: String? = null
    private var lessonIdVar : String? = null
    private var paymentList = mutableListOf<Triple<String,String,String>>()
    private lateinit var binding:FragmentRecordPaymentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecordPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        paymentViewModelFactory = PaymentViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        paymentViewModel = ViewModelProvider(this, paymentViewModelFactory)[PaymentViewModel::class.java]

        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!

        gradeVar=args.gradeLevelId
        groupIdVar=args.groupId
        monthVar=args.month
        groupNameVar=args.groupName


        groupPaymentAdapter=GroupPaymentAdapter(paymentClickLambda)
        recyclerView = binding.paymentRecycler
        recyclerView.apply {
            adapter = groupPaymentAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }


        binding.attendanceTV.text = " ${getString(R.string.record_payment)} $groupNameVar"

        getStudentsPaymentForMonth()

    }
    private fun getStudentsPaymentForMonth(){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getGroupStudents(teacherIdVar!!,groupIdVar!!,semesterVar!!)
                studentsViewModel.getGroupStudent.collect{ result->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.attendanceProgressBar.visibility=View.VISIBLE
                            binding.paymentRecycler.visibility=View.INVISIBLE
                            binding.noStudentsTv.visibility=View.INVISIBLE
                            binding.noDataAnimationView.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()){

                                binding.attendanceProgressBar.visibility=View.INVISIBLE
                                binding.paymentRecycler.visibility=View.INVISIBLE
                                binding.noStudentsTv.visibility=View.VISIBLE
                                binding.noDataAnimationView.visibility=View.VISIBLE

                            }else{
                                Log.i("TAG", "getAllStudents: ${result.data}")
                                paymentViewModel.getStudentsPaymentForMonth(semesterVar!!, gradeVar!!,monthVar!! ,result.data)
                                paymentViewModel.getMonthPayment.collect{ result->
                                    when(result){
                                        is FirebaseState.Loading ->{
                                            Log.i("TAG", "getLessonAttendance: Loading")
                                        }
                                        is FirebaseState.Success ->{
                                            if (result.data.isEmpty()){
                                                Log.i("TAG", "getLessonAttendance: Empty")
                                            }else{
                                                binding.attendanceProgressBar.visibility=View.INVISIBLE
                                                binding.paymentRecycler.visibility=View.VISIBLE
                                                binding.noStudentsTv.visibility=View.INVISIBLE
                                                binding.noDataAnimationView.visibility=View.INVISIBLE
                                                paymentList=result.data.toMutableList()
                                                groupPaymentAdapter.submitList(result.data)
                                            }
                                        }
                                        is FirebaseState.Failure ->{
                                            Log.i("TAG", "getLessonAttendance: Fail")

                                        }

                                    }

                                } }
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

    private val paymentClickLambda = { payment: Triple<String, String, String> ->
        val builder = AlertDialog.Builder(requireContext())
        val paymentDialog = PaymentDialogBinding.inflate(layoutInflater)
        builder.setView(paymentDialog.root)
        val dialog = builder.create()
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        paymentDialog.studentNameTV.text=payment.second

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
    private fun  replaceItem(list: MutableList<Triple<String,String,String>>, oldItem: Triple<String,String,String>, newItem: Triple<String,String,String>) {
        val index = list.indexOf(oldItem)
        if (index != -1) {
            list[index] = newItem
        }
        groupPaymentAdapter.notifyDataSetChanged()

    }
    private fun addStudentAttendance(paymentStatus:String, payment:Triple<String,String,String>){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                paymentViewModel.addStudentPaymentForMonth(semesterVar!!,gradeVar!!,monthVar!!,payment.first,paymentStatus)
                replaceItem(paymentList,payment,Triple(payment.first,payment.second,paymentStatus))
                groupPaymentAdapter.submitList(paymentList)
                Log.i("TAG", ": $paymentList")
            }}
        else{
            Toast.makeText(requireContext(),getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }
}