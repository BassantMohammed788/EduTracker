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
import com.example.edutracker.databinding.FragmentStudentDashAttendanceBinding
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.view.StudentAttendanceAdapter
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.example.edutracker.utilities.sortDatesFromLatestToOldest
import kotlinx.coroutines.launch


class StudentDashAttendanceFragment : Fragment() {
    private lateinit var binding:FragmentStudentDashAttendanceBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var studentAttendanceAdapter: StudentAttendanceAdapter
    private lateinit var recyclerView: RecyclerView
    private var semesterVar: String = ""
    private var teacherId:String=""
    private var studentId:String=""
    private var gradeLevel:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStudentDashAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!
        teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        studentId = MySharedPreferences.getInstance(requireContext()).getStudentID()!!
        gradeLevel = MySharedPreferences.getInstance(requireContext()).getStudentGradeLevel()!!

        studentAttendanceAdapter= StudentAttendanceAdapter()
        recyclerView = binding.attendanceRecycler
        recyclerView.apply {
            adapter = studentAttendanceAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getStudentAttendanceDetails(studentId,semesterVar,gradeLevel)
                studentsViewModel.getStudentAttendanceDetails.collect{result->
                    when(result){
                        is FirebaseState.Loading->{
                            binding.attendanceProgressBar.visibility=View.VISIBLE
                            binding.attendanceRecycler.visibility=View.INVISIBLE
                            binding.noAttendanceTv.visibility=View.INVISIBLE
                            binding.noDataAnimationView.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success->{
                            if (result.data.isEmpty()){
                                binding.attendanceProgressBar.visibility=View.INVISIBLE
                                binding.attendanceRecycler.visibility=View.INVISIBLE
                                binding.noAttendanceTv.visibility=View.VISIBLE
                                binding.noDataAnimationView.visibility=View.VISIBLE
                            }else{
                                studentsViewModel.getLessonTimeAndAttendanceStatus( teacherId,semesterVar,gradeLevel,result.data)
                                studentsViewModel.getLessonTimeAndAttendanceStatus.collect{ result->
                                    when(result){

                                        is FirebaseState.Loading->{
                                            Log.i("TAG", "getLessonTimeAndAttendanceStatus: Loading")
                                        }
                                        is FirebaseState.Success->{
                                            binding.attendanceProgressBar.visibility=View.INVISIBLE
                                            binding.attendanceRecycler.visibility=View.VISIBLE
                                            binding.noAttendanceTv.visibility=View.INVISIBLE
                                            binding.noDataAnimationView.visibility=View.INVISIBLE
                                            studentAttendanceAdapter.submitList(
                                                sortDatesFromLatestToOldest( result.data))
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