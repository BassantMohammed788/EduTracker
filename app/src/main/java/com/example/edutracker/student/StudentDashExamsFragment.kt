package com.example.edutracker.student

import android.os.Bundle
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
import com.example.edutracker.databinding.FragmentStudentDashExamsBinding
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.view.StudentExamsDegreesAdapter
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class StudentDashExamsFragment : Fragment() {

    private lateinit var binding: FragmentStudentDashExamsBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentExamsDegreesAdapter: StudentExamsDegreesAdapter
    private var semesterVar: String = ""
    private var teacherId:String=""
    private var studentId:String=""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentDashExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!
        teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        studentId = MySharedPreferences.getInstance(requireContext()).getStudentID()!!

        studentExamsDegreesAdapter= StudentExamsDegreesAdapter()
        recyclerView = binding.recordExamsRecycler
        recyclerView.apply {
            adapter = studentExamsDegreesAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        getStudentExams()
    }

    private fun getStudentExams(){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getStudentExamsDegrees(studentId,semesterVar)
                studentsViewModel.getExamDegrees.collect{result->
                    when(result){
                        is FirebaseState.Loading->{
                            binding.examsProgressBar.visibility=View.VISIBLE
                            binding.recordExamsRecycler.visibility=View.INVISIBLE
                            binding.noStudentsTv.visibility=View.INVISIBLE
                            binding.noDataAnimationView.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success->{
                            if (result.data.isEmpty()){
                                binding.examsProgressBar.visibility=View.INVISIBLE
                                binding.recordExamsRecycler.visibility=View.INVISIBLE
                                binding.noStudentsTv.visibility=View.VISIBLE
                                binding.noDataAnimationView.visibility=View.VISIBLE
                            }else{
                                binding.examsProgressBar.visibility=View.INVISIBLE
                                binding.recordExamsRecycler.visibility=View.VISIBLE
                                binding.noStudentsTv.visibility=View.INVISIBLE
                                binding.noDataAnimationView.visibility=View.INVISIBLE
                                studentExamsDegreesAdapter.submitList(result.data)

                            }
                        }
                        else->{}
                    }
                }
            }
        }else{
            Toast.makeText(requireContext(),getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }



}