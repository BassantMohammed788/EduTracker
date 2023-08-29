package com.example.edutracker.teacher.students.view

import GradeLevelAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentAddNewGroupBinding
import com.example.edutracker.databinding.FragmentAddStudentBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.teacher.lessons.view.GroupAdapter
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModel
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModelFactory
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.*
import kotlinx.coroutines.launch

class AddStudentFragment : Fragment() {
    private lateinit var binding:FragmentAddStudentBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter
    private var gradeVar: String? = null
    private lateinit var groupsAdapter: GroupAdapter
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var teacherIdVar : String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        groupsViewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        groupsViewModel = ViewModelProvider(this, groupsViewModelFactory)[GroupsViewModel::class.java]

        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()

        gradeLevelAdapter = GradeLevelAdapter(emptyList(),gradeLambda)
        groupsAdapter=GroupAdapter(groupClickLambda)

        binding.chooseLevel.setOnClickListener {
            if (semesterVar != null) {
                displayGradeLevelDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_semester),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.chooseGroup.setOnClickListener {
            if (gradeVar != null) {
                displayGroupDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_grade_level),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack()
            }
        }

        binding.AddAssistantButton.setOnClickListener {

            val studentEmail = binding.studentEmailET.text.toString()
            val studentName = binding.studentNameET.text.toString()
            val studentPhone= binding.studentPhoneNumberET.text.toString()
            val studentPassword= binding.studentPasswordET.text.toString()
            val parentEmail = binding.parentEmailET.text.toString()
            val parentPhone= binding.parentPhoneNumberET.text.toString()
            val parentPassword= binding.parentPasswordET.text.toString()

            val transformedEmail = studentEmail.replace(".", ",")
            if (checkConnectivity(requireContext())){
                if (studentName.isNotEmpty()&&studentEmail.isNotEmpty()&&studentPassword.isNotEmpty()&&studentPhone.isNotEmpty() &&parentEmail.isNotEmpty()&&parentPassword.isNotEmpty()&&parentPhone.isNotEmpty()&&gradeVar!=null&&groupIdVar!=null){
                    if (isValidEmail(studentEmail)){
                        if (checkEgyptianPhoneNumber(studentPhone)){
                            val student = Student(transformedEmail,studentName,teacherIdVar!!,studentPassword,studentPhone,parentEmail,parentPassword,parentPhone,gradeVar!!,groupIdVar!!)
                            lifecycleScope.launch {
                                studentsViewModel.addStudent(student,teacherIdVar!!,semesterVar!!,gradeVar!!,groupIdVar!!)
                                studentsViewModel.addStudent.collect { result ->
                                    when (result) {
                                        is FirebaseState.Loading -> {
                                            Log.i("TAG", "onViewCreated: loading")
                                        }
                                        is FirebaseState.Success -> {
                                            if (result.data) {
                                                Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show()
                                                Navigation.findNavController(requireView()).apply {
                                                    popBackStack() // Clear the back stack up to teacherAllAssistantFragment
                                                }
                                            } else {
                                                Toast.makeText(requireContext(), "The email already exists", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        is FirebaseState.Failure -> {
                                            Toast.makeText(requireContext(), "Error: ${result.msg}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                        }else{
                            Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(requireContext(), getString(R.string.emailNotValid), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()

                }
            }else{
                Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

            }




        }

    }
    private fun displayGroupDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = groupsAdapter
        gradeLevelDialog.GradeTv.text = getString(R.string.choose_group)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())) {
            if (gradeLevel(gradeVar) != null) {
                lifecycleScope.launch {
                    groupsViewModel.getAllGroups(
                        semesterVar!!,
                        MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                        gradeLevel(gradeVar)!!
                    )
                    groupsViewModel.getGroups.collect { result ->
                        when (result) {
                            is FirebaseState.Loading -> {
                                gradeLevelDialog.progressBar.visibility = View.VISIBLE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE
                            }
                            is FirebaseState.Success -> {
                                if (result.data.isEmpty()) {
                                    gradeLevelDialog.progressBar.visibility = View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility = View.INVISIBLE
                                    gradeLevelDialog.noDataTv.visibility = View.VISIBLE
                                    gradeLevelDialog.noDataTv.text =
                                        getString(R.string.no_groups_yet)
                                } else {
                                    gradeLevelDialog.progressBar.visibility = View.GONE
                                    gradeLevelDialog.noDataTv.visibility = View.INVISIBLE
                                    gradeLevelDialog.GradeLevelRecycler.visibility = View.VISIBLE
                                    groupsAdapter.submitList(result.data)
                                }

                            }
                            is FirebaseState.Failure -> {}

                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_semester_level),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.network_lost_title),
                Toast.LENGTH_SHORT
            ).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            binding.gradeName.text = gradeVar
            dialog.dismiss()
        }
    }

    private fun displayGradeLevelDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = gradeLevelAdapter
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())) {
            lifecycleScope.launch {
                groupsViewModel.getAllGrades(
                    semesterVar!!,
                    MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
                )
                groupsViewModel.getGrades.collect { result ->
                    when (result) {
                        is FirebaseState.Loading -> {
                            gradeLevelDialog.progressBar.visibility = View.VISIBLE
                            gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()) {
                                gradeLevelDialog.progressBar.visibility = View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE
                                gradeLevelDialog.noDataTv.visibility = View.VISIBLE
                                gradeLevelDialog.noDataTv.text = getString(R.string.no_grades_yet)
                            } else {
                                gradeLevelDialog.progressBar.visibility = View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.VISIBLE
                                gradeLevelDialog.noDataTv.visibility = View.INVISIBLE
                                val list = mutableListOf<String>()
                                for (i in result.data) {
                                    list.add(gradeLevel(i)!!)
                                }
                                gradeLevelAdapter.setGradeLevelsList(list)
                            }

                        }
                        is FirebaseState.Failure -> {}

                    }

                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.network_lost_title),
                Toast.LENGTH_SHORT
            ).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            binding.gradeName.text = gradeVar
            dialog.dismiss()
        }
    }

    private val gradeLambda = { string: String ->
        gradeVar = string
    }

    private val groupClickLambda = { group: Group ->

        groupNameVar = group.name
        Log.i("TAG", "groupVar:$groupNameVar ")
        binding.groupName.text = groupNameVar
        groupIdVar = group.id
    }
}