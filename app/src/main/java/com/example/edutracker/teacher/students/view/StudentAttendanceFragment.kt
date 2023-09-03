package com.example.edutracker.teacher.students.view

import android.os.Bundle
import android.util.Log
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
import com.example.edutracker.databinding.FragmentStudentAttendanceBinding
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class StudentAttendanceFragment : Fragment() {
    private lateinit var binding:FragmentStudentAttendanceBinding

    private val args: StudentAttendanceFragmentArgs by navArgs()
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var studentAttendanceAdapter: StudentAttendanceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var student: Student
    private var semesterVar: String = ""
    private var teacherId:String=""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        student=args.student
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!
        teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!

        studentAttendanceAdapter= StudentAttendanceAdapter()
        recyclerView = binding.attendanceRecycler
        recyclerView.apply {
            adapter = studentAttendanceAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        binding.attendanceTV.text=student.name
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getStudentAttendanceDetails(student.email,semesterVar!!,student.activeGradeLevel)
                studentsViewModel.getStudentAttendanceDetails.collect{result->
                    when(result){
                        is FirebaseState.Loading->{
                            binding.attendanceProgressBar.visibility=View.VISIBLE
                            binding.attendanceRecycler.visibility=View.INVISIBLE
                            binding.noAttendanceTv.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success->{
                            if (result.data.isEmpty()){
                                binding.attendanceProgressBar.visibility=View.INVISIBLE
                                binding.attendanceRecycler.visibility=View.INVISIBLE
                                binding.noAttendanceTv.visibility=View.VISIBLE
                            }else{
                                studentsViewModel.getLessonTimeAndAttendanceStatus( teacherId,semesterVar,student.activeGradeLevel,result.data)

                                studentsViewModel.getLessonTimeAndAttendanceStatus.collect{ result->
                                    when(result){

                                        is FirebaseState.Loading->{
                                            Log.i("TAG", "getLessonTimeAndAttendanceStatus: Loading")
                                        }
                                        is FirebaseState.Success->{
                                            binding.attendanceProgressBar.visibility=View.INVISIBLE
                                            binding.attendanceRecycler.visibility=View.VISIBLE
                                            binding.noAttendanceTv.visibility=View.INVISIBLE
                                            studentAttendanceAdapter.submitList(result.data)
                                        }
                                        is FirebaseState.Failure ->{
                                            Log.i("TAG", "getLessonTimeAndAttendanceStatus: Fail")

                                        }
                                    }
                                }
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