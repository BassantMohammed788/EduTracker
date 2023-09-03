package com.example.edutracker.teacher.students.view

import android.os.Bundle
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
import com.example.edutracker.databinding.FragmentStudentExamDegreesBinding
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class StudentExamDegreesFragment : Fragment() {
    private lateinit var binding:FragmentStudentExamDegreesBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private val args: StudentExamDegreesFragmentArgs by navArgs()
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentExamsDegreesAdapter:StudentExamsDegreesAdapter
    private lateinit var student: Student
    private var semesterVar: String = ""
    private var teacherId:String=""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentExamDegreesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        student=args.student
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!
        teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!

        studentExamsDegreesAdapter= StudentExamsDegreesAdapter()
        recyclerView = binding.recordExamsRecycler
        recyclerView.apply {
            adapter = studentExamsDegreesAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        binding.recordExamsTV.text=student.name

        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getStudentExamsDegrees(student.email,semesterVar)
                studentsViewModel.getExamDegrees.collect{result->
                    when(result){
                        is FirebaseState.Loading->{
                            binding.examsProgressBar.visibility=View.VISIBLE
                            binding.recordExamsRecycler.visibility=View.INVISIBLE
                            binding.noStudentsTv.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success->{
                            if (result.data.isEmpty()){
                                binding.examsProgressBar.visibility=View.INVISIBLE
                                binding.recordExamsRecycler.visibility=View.INVISIBLE
                                binding.noStudentsTv.visibility=View.VISIBLE
                            }else{
                                binding.examsProgressBar.visibility=View.INVISIBLE
                                binding.recordExamsRecycler.visibility=View.VISIBLE
                                binding.noStudentsTv.visibility=View.INVISIBLE
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