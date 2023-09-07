package com.example.edutracker.student

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentStudentDashPaymentBinding
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.view.StudentPaymentAdapter
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class StudentDashPaymentFragment : Fragment() {
    private lateinit var binding:FragmentStudentDashPaymentBinding

    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var studentPaymentAdapter: StudentPaymentAdapter
    private lateinit var recyclerView: RecyclerView
    private var semesterVar: String? = null
    private var studentId=""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding =FragmentStudentDashPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        studentPaymentAdapter=StudentPaymentAdapter(paymentClickLambda)
        recyclerView = binding.paymentRecycler
        recyclerView.apply {
            adapter = studentPaymentAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()
        studentId=MySharedPreferences.getInstance(requireContext()).getStudentID()!!
        getStudentPaymentStatusForAllMonths()


    }


    private fun getStudentPaymentStatusForAllMonths(){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getStudentPaymentStatusForAllMonths(studentId,semesterVar!!)
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

    }
}