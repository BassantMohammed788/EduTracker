package com.example.edutracker.teacher.students.view

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
import androidx.navigation.fragment.navArgs
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentStudentDetailsBinding
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
import com.example.edutracker.utilities.*
import kotlinx.coroutines.launch

class StudentDetailsFragment : Fragment() {
    lateinit var binding:FragmentStudentDetailsBinding
    private val args: StudentDetailsFragmentArgs by navArgs()
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory
    private lateinit var groupsAdapter: GroupAdapter
    private lateinit var student : Student
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var groupList = emptyList<Group>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        groupsViewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        groupsViewModel = ViewModelProvider(this, groupsViewModelFactory)[GroupsViewModel::class.java]

        groupsAdapter=GroupAdapter(groupClickLambda)

        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()
        student = args.student
        groupIdVar=student.activeGroupId


        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                groupsViewModel.getAllGroups(semesterVar!!,
                    MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                    student.activeGradeLevel)
                groupsViewModel.getGroups.collect{ result->
                    when(result){
                        is FirebaseState.Loading ->{
                            binding.progressBar.visibility=View.VISIBLE
                            binding.constraint.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success ->{
                                binding.progressBar.visibility=View.GONE
                                binding.constraint.visibility=View.VISIBLE
                                 groupList=result.data
                                setUpStudentData()
                        }
                        is FirebaseState.Failure ->{}

                    }
                }
            }
        }else{
            binding.constraint.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack()
            }
        }
        binding.groupET.setOnClickListener {
            displayGroupDialog()
        }
        binding.updateButton.setOnClickListener {
            val studentPhone = binding.studentPhoneET.text.toString()
            val studentName = binding.studentNameET.text.toString()
            val studentPassword = binding.studentPasswordET.text.toString()
          if (checkConnectivity(requireContext())){
              if (studentName.isNotEmpty() && studentPhone.isNotEmpty() && studentPassword.isNotEmpty() ) {
                  if ( checkEgyptianPhoneNumber(studentPhone)) {
                      val updatedStudent=Student(student.email,studentName,student.teacherId,studentPassword,studentPhone,student.activeGradeLevel,groupIdVar!!,semesterVar!!)

                      if (updatedStudent==student){
                          Toast.makeText(requireContext(), getString(R.string.no_thing_change), Toast.LENGTH_SHORT).show()
                      }else{
                        updateStudent(updatedStudent)
                      }

                  } else {
                      Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                  }
              } else {
                  Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()
              }
          }else{
              Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

          }



        }
    }
    private fun updateStudent(updatedStudent:Student){
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                studentsViewModel.updateStudent(updatedStudent)
                studentsViewModel.updateStudent.collect{ result->
                    when(result){
                        is FirebaseState.Loading->{
                            dialog.show()
                        }
                        is FirebaseState.Success->{
                            dialog.dismiss()
                            if (result.data){
                                if (groupIdVar!=student.activeGroupId) {
                                    Toast.makeText(requireContext(), getString(R.string.updated_successfully_and_move_to_new_group), Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(requireContext(),getString(R.string.updated_successfully),Toast.LENGTH_SHORT).show()
                                }
                                Navigation.findNavController(requireView()).apply {
                                    popBackStack()
                                }
                            }else{
                                dialog.dismiss()
                                Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
                            }
                        }
                        is FirebaseState.Failure->{
                            dialog.dismiss()
                        }
                    }
                }
            }}else{
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }

    }
    private fun setUpStudentData(){
        binding.studentEmailETt.isFocusableInTouchMode = false
        binding.gradeLevelET.isFocusableInTouchMode = false
        binding.groupET.isFocusableInTouchMode = false

        binding.studentEmailETt.setText(student.email.replace(",", "."))
        binding.studentNameET.setText(student.name)
        binding.studentPhoneET.setText(student.phoneNumber)
        binding.studentPasswordET.setText(student.password)
        binding.gradeLevelET.setText(student.activeGradeLevel)
        for (i in groupList) {
            if (i.id == student.activeGroupId) {
                groupNameVar = i.name
                binding.groupET.setText(i.name)
            }
        }
    }
    private val groupClickLambda = { group: Group ->
        groupNameVar = group.name
        Log.i("TAG", ":$groupNameVar ")
        groupIdVar = group.id
    }
    private fun displayGroupDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = groupsAdapter
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_group)
        groupsAdapter.submitList(groupList)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        gradeLevelDialog.okBTN.setOnClickListener {
            binding.groupET.setText(groupNameVar)
            dialog.dismiss()
        }
    }

}