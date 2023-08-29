package com.example.edutracker.teacher.attendance.view

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentTakeAttendanceBinding
import com.example.edutracker.databinding.TakeAttendanceDialogBinding
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.attendance.viewmodel.AttendanceViewModel
import com.example.edutracker.teacher.attendance.viewmodel.AttendanceViewModelFactory
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch

@Suppress("NAME_SHADOWING")
class TakeAttendanceFragment : Fragment() {
    lateinit var binding:FragmentTakeAttendanceBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var attendanceViewModelFactory: AttendanceViewModelFactory
    private lateinit var groupAttendanceAdapter: GroupAttendanceAdapter
    lateinit var recyclerView: RecyclerView
    private val args: TakeAttendanceFragmentArgs by navArgs()
    private var gradeVar: String? = null
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var monthVar: String? = null
    private var teacherIdVar: String? = null
    private var lessonIdVar : String? = null
    private var attendanceList = mutableListOf<Triple<String,String,String>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTakeAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        attendanceViewModelFactory = AttendanceViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        attendanceViewModel = ViewModelProvider(this, attendanceViewModelFactory)[AttendanceViewModel::class.java]

        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!

        gradeVar=args.gradeLevelId
        groupIdVar=args.groupId
        monthVar=args.month
        lessonIdVar=args.lessonId
        groupNameVar=args.groupName


        groupAttendanceAdapter=GroupAttendanceAdapter(attendanceClickLambda)
        recyclerView = binding.attendanceRecycler
        recyclerView.apply {
            adapter = groupAttendanceAdapter
            layoutManager = GridLayoutManager(context, 3)
        }

        binding.attendanceTV.text = "$groupNameVar ${getString(R.string.attendance)}"

        getGroupStudentsAttendance()

    }
    private fun getGroupStudentsAttendance(){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getAllStudents(teacherIdVar!!)
                studentsViewModel.getStudent.collect{ result->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.attendanceProgressBar.visibility=View.VISIBLE
                            binding.attendanceRecycler.visibility=View.INVISIBLE
                            binding.noStudentsTv.visibility=View.INVISIBLE

                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()){

                                binding.attendanceProgressBar.visibility=View.INVISIBLE
                                binding.attendanceRecycler.visibility=View.INVISIBLE
                                binding.noStudentsTv.visibility=View.VISIBLE
                            }else{
                                Log.i("TAG", "getAllStudents: ${result.data}")
                                attendanceViewModel.getLessonAttendance(semesterVar!!, gradeVar!!,groupIdVar!!,lessonIdVar!!,monthVar!!,result.data)
                                attendanceViewModel.getLessonAttendance.collect{ result->
                                    when(result){
                                        is FirebaseState.Loading ->{
                                            Log.i("TAG", "getLessonAttendance: Loading")
                                        }
                                        is FirebaseState.Success ->{
                                            if (result.data.isEmpty()){
                                                Log.i("TAG", "getLessonAttendance: Empty")
                                            }else{
                                                binding.attendanceProgressBar.visibility=View.INVISIBLE
                                                binding.attendanceRecycler.visibility=View.VISIBLE
                                                binding.noStudentsTv.visibility=View.INVISIBLE
                                                attendanceList=result.data.toMutableList()
                                                groupAttendanceAdapter.submitList(result.data)
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
            Toast.makeText(requireContext(),getString(R.string.network_lost_title),Toast.LENGTH_SHORT).show()

        }
    }

    private val attendanceClickLambda = { attendance: Triple<String, String, String> ->
        val builder = AlertDialog.Builder(requireContext())
        val attendanceDialog = TakeAttendanceDialogBinding.inflate(layoutInflater)
        builder.setView(attendanceDialog.root)
        val dialog = builder.create()
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        attendanceDialog.studentNameTV.text=attendance.second
        attendanceDialog.lateCard.setOnClickListener {
            addStudentAttendance(getString(R.string.late),attendance)
            dialog.dismiss()
        }
        attendanceDialog.presentCard.setOnClickListener {
            addStudentAttendance(getString(R.string.present),attendance)
            dialog.dismiss()
        }
        attendanceDialog.permittedCard.setOnClickListener {
            addStudentAttendance(getString(R.string.permitted),attendance)
            dialog.dismiss()
        }
        attendanceDialog.absentCard.setOnClickListener {
        addStudentAttendance(getString(R.string.absent),attendance)
            dialog.dismiss()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun  replaceItem(list: MutableList<Triple<String,String,String>>, oldItem: Triple<String,String,String>, newItem: Triple<String,String,String>) {
        val index = list.indexOf(oldItem)
        if (index != -1) {
            list[index] = newItem
        }
        groupAttendanceAdapter.notifyDataSetChanged()

    }
    private fun addStudentAttendance(attendanceStatus:String, attendance:Triple<String,String,String>){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                attendanceViewModel.addStudentAttendanceForLesson(semesterVar!!,gradeVar!!,groupIdVar!!,lessonIdVar!!,monthVar!!,attendance.first,attendanceStatus)
                replaceItem(attendanceList,attendance,Triple(attendance.first,attendance.second,attendanceStatus))
                groupAttendanceAdapter.submitList(attendanceList)
                Log.i("TAG", ": $attendanceList")
            }}
        else{
            Toast.makeText(requireContext(),getString(R.string.network_lost_title),Toast.LENGTH_SHORT).show()
        }
    }
}