package com.example.edutracker.teacher.students.view

import GradeLevelAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.edutracker.databinding.ChooseStudentDialogBinding
import com.example.edutracker.databinding.FragmentAddExistingStudentBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.teacher.lessons.view.GroupAdapter
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch

class AddExistingStudentFragment : Fragment() {
    private lateinit var binding: FragmentAddExistingStudentBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter
    private lateinit var allStudentsAdapter:AllStudentsAdapter
    private lateinit var oldStudent: Student
    private  var studentName:String?=null
    private var gradeVar: String? = null
    private lateinit var groupsAdapter: GroupAdapter
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var teacherIdVar : String? = null

    private var studentsList: List<Student> = listOf()
    private var filteredStudentsList: List<Student> = listOf()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddExistingStudentBinding.inflate(inflater, container, false)
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
        allStudentsAdapter=AllStudentsAdapter(studentClickLambda)
        binding.chooseStudent.setOnClickListener {
            displayAllStudentsDialog()
        }
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
            Toast.makeText(
                requireContext(),
                getString(R.string.you_should_choose_grade_level),
                Toast.LENGTH_SHORT
            ).show()
        /*  if (gradeVar != null) {
                displayGroupDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_grade_level),
                    Toast.LENGTH_SHORT
                ).show()
            }*/
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack()
            }
        }
        binding.AddAssistantButton.setOnClickListener {
            if (gradeVar!=null&&groupIdVar!=null&&studentName!=null){
                val newStudentData=Student(oldStudent.email,oldStudent.name,teacherIdVar!!,oldStudent.password,oldStudent.phoneNumber,gradeVar!!,groupIdVar!!,semesterVar!!)

                if (checkConnectivity(requireContext())){

                    lifecycleScope.launch {
                        val builder = AlertDialog.Builder(requireContext())
                        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
                        builder.setView(loadingDialogB.root)
                        val dialog = builder.create()
                        dialog.setCancelable(false)
                        studentsViewModel.addStudentToNewSemester(newStudentData)
                        studentsViewModel.addStudentToNewSemester.collect { result ->
                            when (result) {
                                is FirebaseState.Loading -> {
                                    dialog.show()
                                    Log.i("TAG", "onViewCreated: loading")
                                }
                                is FirebaseState.Success -> {
                                    dialog.dismiss()
                                    if (result.data) {
                                        Toast.makeText(requireContext(), getString(R.string.added_successfully), Toast.LENGTH_SHORT).show()
                                        Navigation.findNavController(requireView()).apply {
                                            popBackStack() // Clear the back stack up to teacherAllAssistantFragment
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.The_email_already_exists), Toast.LENGTH_SHORT).show()
                                    }
                                }
                                is FirebaseState.Failure -> {
                                    dialog.dismiss()
                                    Toast.makeText(requireContext(), "Error: ${result.msg}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }else{
                    Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

                }
            }else{
                Toast.makeText(requireContext(), getString(R.string.you_must_chhose_student_and_grade_and_group), Toast.LENGTH_SHORT).show()

            }
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
                            gradeLevelDialog.noDataTv.visibility = View.INVISIBLE
                            gradeLevelDialog.noDataAnimationView.visibility = View.INVISIBLE
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()) {
                                gradeLevelDialog.progressBar.visibility = View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE
                                gradeLevelDialog.noDataTv.visibility = View.VISIBLE
                                gradeLevelDialog.noDataAnimationView.visibility = View.VISIBLE
                                gradeLevelDialog.noDataTv.text = getString(R.string.no_grades_yet)
                            } else {
                                gradeLevelDialog.progressBar.visibility = View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.VISIBLE
                                gradeLevelDialog.noDataTv.visibility = View.INVISIBLE
                                gradeLevelDialog.noDataAnimationView.visibility = View.INVISIBLE
                                val list = mutableListOf<String>()
                                for (i in result.data) {
                                    list.add(i)
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
            if (gradeVar!=null){
                binding.gradeName.text = gradeVar
                displayGroupDialog()
            }
            dialog.dismiss()
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
            if (gradeVar != null) {
                lifecycleScope.launch {
                    groupsViewModel.getAllGroups(
                        semesterVar!!,
                        MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                        gradeVar!!
                    )
                    groupsViewModel.getGroups.collect { result ->
                        when (result) {
                            is FirebaseState.Loading -> {
                                gradeLevelDialog.progressBar.visibility = View.VISIBLE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE
                                gradeLevelDialog.noDataTv.visibility = View.INVISIBLE
                                gradeLevelDialog.noDataAnimationView.visibility = View.INVISIBLE
                            }
                            is FirebaseState.Success -> {
                                if (result.data.isEmpty()) {
                                    gradeLevelDialog.progressBar.visibility = View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility = View.INVISIBLE

                                    gradeLevelDialog.noDataTv.visibility = View.VISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility = View.VISIBLE
                                    gradeLevelDialog.noDataTv.text =
                                        getString(R.string.no_groups_yet)
                                } else {
                                    gradeLevelDialog.progressBar.visibility = View.GONE
                                    gradeLevelDialog.noDataTv.visibility = View.INVISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility = View.INVISIBLE
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
            if (gradeVar!=null) {
                binding.gradeName.text = gradeVar
            }
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
    private val studentClickLambda = { student: Student ->
        studentName = student.name
        Log.i("TAG", "groupVar:$groupNameVar ")
        binding.studentName.text = studentName
        oldStudent = student
    }

    private fun displayAllStudentsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val chooseStudentsDialog = ChooseStudentDialogBinding.inflate(layoutInflater)
        chooseStudentsDialog.studentsRecycler.adapter = allStudentsAdapter
        builder.setView(chooseStudentsDialog.root)
        val dialog = builder.create()
        dialog.show()
        val searchEditText = chooseStudentsDialog.searchEditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterStudents(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement anything here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No need to implement anything here
            }
        })
        if (checkConnectivity(requireContext())) {
            lifecycleScope.launch {
                studentsViewModel.getAllExistingStudents(teacherIdVar!!,semesterVar!!)
                studentsViewModel.getExistingStudents.collect { result ->
                    when (result) {
                        is FirebaseState.Loading -> {
                            chooseStudentsDialog.progressBar.visibility = View.VISIBLE
                            chooseStudentsDialog.studentsRecycler.visibility = View.INVISIBLE
                            chooseStudentsDialog.noDataTv.visibility = View.INVISIBLE
                            chooseStudentsDialog.noDataAnimationView.visibility = View.INVISIBLE
                            chooseStudentsDialog.searchEditText.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()) {
                                chooseStudentsDialog.progressBar.visibility = View.GONE
                                chooseStudentsDialog.studentsRecycler.visibility = View.INVISIBLE
                                chooseStudentsDialog.noDataTv.visibility = View.VISIBLE
                                chooseStudentsDialog.noDataAnimationView.visibility = View.VISIBLE
                                chooseStudentsDialog.searchEditText.visibility=View.INVISIBLE
                            } else {
                                chooseStudentsDialog.progressBar.visibility = View.INVISIBLE
                                chooseStudentsDialog.studentsRecycler.visibility = View.VISIBLE
                                chooseStudentsDialog.noDataTv.visibility = View.INVISIBLE
                                chooseStudentsDialog.noDataAnimationView.visibility = View.INVISIBLE
                                chooseStudentsDialog.searchEditText.visibility=View.VISIBLE
                                studentsList=result.data
                                allStudentsAdapter.submitList(result.data)
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
        chooseStudentsDialog.okBTN.setOnClickListener {
            if (studentName!=null){
                binding.studentName.text = studentName
            }
            dialog.dismiss()
        }
    }
    private fun filterStudents(filterText: String) {
        filteredStudentsList = studentsList.filter { student ->
            student.name.contains(filterText, ignoreCase = true)
        }
        allStudentsAdapter.submitList(filteredStudentsList)
    }
}