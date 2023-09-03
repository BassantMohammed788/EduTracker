package com.example.edutracker.teacher.exams.view

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
import com.example.edutracker.databinding.AddExamDegreeDialogBinding
import com.example.edutracker.databinding.FragmentRecordExamDegreesBinding
import com.example.edutracker.dataclasses.Exam
import com.example.edutracker.dataclasses.ExamDegree
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.exams.viewmodel.ExamsViewModel
import com.example.edutracker.teacher.exams.viewmodel.ExamsViewModelFactory
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class RecordExamDegreesFragment : Fragment() {
    private lateinit var binding: FragmentRecordExamDegreesBinding

    private val args: RecordExamDegreesFragmentArgs by navArgs()
    private lateinit var examViewModel: ExamsViewModel
    private lateinit var examViewModelFactory: ExamsViewModelFactory
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var examDegreeAdapter: ExamDegreeAdapter
    private lateinit var recyclerView: RecyclerView
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var teacherIdVar: String? = null
    private var gradeLevelVar:String?=null
    lateinit var examObj:Exam
    private var examDegreesList = mutableListOf<Triple<String,String,ExamDegree>>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecordExamDegreesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        examViewModelFactory = ExamsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        examViewModel = ViewModelProvider(this, examViewModelFactory)[ExamsViewModel::class.java]

        examDegreeAdapter=ExamDegreeAdapter(examDegreeClickLambda)
        recyclerView = binding.recordExamsRecycler
        recyclerView.apply {
            adapter = examDegreeAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()
        teacherIdVar =MySharedPreferences.getInstance(requireContext()).getTeacherID()

        groupIdVar=args.groupId
        gradeLevelVar=args.gradeLevel
        groupNameVar=args.groupName
        examObj=args.exam

        binding.recordExamsTV.text = examObj.examName
        getStudentsExamDegreeForGroup()
    }


    private fun getStudentsExamDegreeForGroup(){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.getGroupStudents(teacherIdVar!!,groupIdVar!!,semesterVar!!)
                studentsViewModel.getGroupStudent.collect{ result->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.examsProgressBar.visibility=View.VISIBLE
                            binding.recordExamsRecycler.visibility=View.INVISIBLE
                            binding.noStudentsTv.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()){
                                binding.examsProgressBar.visibility=View.INVISIBLE
                                binding.recordExamsRecycler.visibility=View.INVISIBLE
                                binding.noStudentsTv.visibility=View.VISIBLE
                                Log.i("TAG", "NoStudents: ")

                            }else{
                                Log.i("TAG", "getAllStudents: ${result.data}")
                                examViewModel.getStudentsExamDegreeForGroup(semesterVar!!, gradeLevelVar!!,groupIdVar!!,examObj.id ,result.data)
                                examViewModel.getExamDegrees.collect{ result->
                                    when(result){
                                        is FirebaseState.Loading ->{
                                            Log.i("TAG", "getLessonAttendance: Loading")
                                        }
                                        is FirebaseState.Success ->{
                                            if (result.data.isEmpty()){
                                                Log.i("TAG", "getLessonAttendance: Empty")
                                            }else{
                                                binding.examsProgressBar.visibility=View.INVISIBLE
                                                binding.recordExamsRecycler.visibility=View.VISIBLE
                                                binding.noStudentsTv.visibility=View.INVISIBLE
                                                examDegreesList=result.data.toMutableList()
                                                examDegreeAdapter.submitList(result.data)
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

    private val examDegreeClickLambda = { exam: Triple<String, String, ExamDegree> ->
        val builder = AlertDialog.Builder(requireContext())
        val examDegreeDialog = AddExamDegreeDialogBinding.inflate(layoutInflater)
        builder.setView(examDegreeDialog.root)
        val dialog = builder.create()
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        examDegreeDialog.studentNameTV.text=exam.second

        examDegreeDialog.rAddButton.setOnClickListener {
            val degree = examDegreeDialog.examDegreeEditText.text.toString()
            if(degree.isNotEmpty()){
                addStudentExamDegree(degree,exam)
                dialog.dismiss()
            }else{
                Toast.makeText(requireContext(),getString(R.string.enter_exam_degree),Toast.LENGTH_SHORT).show()
            }
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun  replaceItem(list: MutableList<Triple<String,String,ExamDegree>>, oldItem: Triple<String,String,ExamDegree>, newItem: Triple<String,String,ExamDegree>) {
        val index = list.indexOf(oldItem)
        if (index != -1) {
            list[index] = newItem
        }
        examDegreeAdapter.notifyDataSetChanged()

    }
    private fun addStudentExamDegree( degree:String,examDegreeItem:Triple<String,String,ExamDegree>){
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                examViewModel.addExamDegree(semesterVar!!,gradeLevelVar!!,examObj.id,examDegreeItem.first, ExamDegree(degree,examObj.examName))
                examViewModel.addExamDegrees.collect{ result->
                    when(result){
                        is FirebaseState.Loading->{
                        }
                        is FirebaseState.Success->{
                            replaceItem(examDegreesList,examDegreeItem,Triple(examDegreeItem.first,examDegreeItem.second,
                                ExamDegree(degree,examObj.examName)
                            ))
                            examDegreeAdapter.submitList(examDegreesList)
                            Log.i("TAG", ": $examDegreesList")
                        }
                        is FirebaseState.Failure->{
                            Toast.makeText(requireContext(),getString(R.string.network_lost_title),Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }}
        else{
            Toast.makeText(requireContext(),getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }}