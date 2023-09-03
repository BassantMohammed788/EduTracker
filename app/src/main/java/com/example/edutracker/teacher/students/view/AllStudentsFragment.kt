package com.example.edutracker.teacher.students.view

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.*
import com.example.edutracker.databinding.FragmentAllStudentsBinding
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class AllStudentsFragment : Fragment() {
    private lateinit var binding:FragmentAllStudentsBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private  lateinit var recyclerView: RecyclerView
    private lateinit var studentsAdapter:StudentsAdapter
    private var teacherIdVar : String? = null
    private var studentsList: List<Student> = listOf()
    private var filteredStudentsList: List<Student> = listOf()
    private var semester : String=""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAllStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]


        studentsAdapter = StudentsAdapter(studentClickLambda)
        recyclerView = binding.studentsRecycler
        recyclerView.apply {
            adapter = studentsAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()
        semester=MySharedPreferences.getInstance(requireContext()).getSemester()!!
        binding.addStudentFAB.setOnClickListener {
           displayNewOrOldStudent()
        }

        lifecycleScope.launch {
            if (checkConnectivity(requireContext())){
                studentsViewModel.getAllStudents(teacherIdVar!!,semester)
                studentsViewModel.getStudent.collect{ result->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.studentsProgressBar.visibility=View.VISIBLE
                            binding.noStudentsTv.visibility=View.GONE
                            Log.i("TAG", "onViewCreated: loading")
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()){
                                binding.studentsProgressBar.visibility=View.GONE
                                binding.noStudentsTv.visibility=View.VISIBLE
                                Log.i("TAG", "onViewCreated: no assistant yet")
                            }else{
                                studentsAdapter.submitList(result.data)
                                studentsList=result.data
                                binding.noStudentsTv.visibility=View.GONE
                                binding.studentsProgressBar.visibility=View.GONE
                                Log.i("TAG", "onViewCreated: ${result.data[0].name}")
                            }
                        }
                        is FirebaseState.Failure -> {
                            binding.noStudentsTv.visibility=View.GONE
                            Log.i("TAG", "onViewCreated: fail")
                        }
                    }
                }

            }else{
                binding.constraintLayout.visibility=View.GONE
                Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

            }
        }


        val searchEditText = binding.searchEditText
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
    }

    private fun displayNewOrOldStudent(){
        val builder = AlertDialog.Builder(requireContext())
        val newOrOldStudentDialog = NewOrOldStudentDialogBinding.inflate(layoutInflater)
        builder.setView(newOrOldStudentDialog.root)
        val dialog = builder.create()
        dialog.show()
        newOrOldStudentDialog.dialogExistingBtn.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToAddExistingStudentFragment()
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }
        newOrOldStudentDialog.dialogNewBtn.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToAddStudentFragment()
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }
    }
    private val studentClickLambda ={ student:Student ->
        val builder = AlertDialog.Builder(requireContext())
        val studentChoicesDialog = StudentChoiceDialogBinding.inflate(layoutInflater)
        builder.setView(studentChoicesDialog.root)
        val dialog = builder.create()
        dialog.show()
        studentChoicesDialog.updateStudentCard.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToStudentDetailsFragment(student)
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }
        studentChoicesDialog.deleteCard.setOnClickListener {
            deleteStudent(student.email)
            dialog.dismiss()
        }
        studentChoicesDialog.studentPaymentCard.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToStudentPaymentFragment(student)
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }
        studentChoicesDialog.studentAttendanceCard.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToStudentAttendanceFragment(student)
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }
        studentChoicesDialog.studentExamsCard.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToStudentExamDegreesFragment(student)
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }
        studentChoicesDialog.parentCard.setOnClickListener {
            val action = AllStudentsFragmentDirections.actionAllStudentsFragmentToParentFragment(student.email,student.name)
            Navigation.findNavController(requireView()).navigate(action)
            dialog.dismiss()
        }


    }
    @SuppressLint("NotifyDataSetChanged")
    private fun deleteStudent(email:String){
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = AlertDialogBinding.inflate(layoutInflater)
        builder.setView(alertDialog.root)
        val dialog = builder.create()
        dialog.show()
        alertDialog.dialogYesBtn.setOnClickListener {
            if (checkConnectivity(requireContext())){
                studentsViewModel.deleteStudent(email)
                studentsAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(),getString(R.string.deleted_successfully),Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),getString(R.string.network_lost_title),Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        alertDialog.dialogNoBtn.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun filterStudents(filterText: String) {
        filteredStudentsList = studentsList.filter { student ->
            student.name.contains(filterText, ignoreCase = true)
        }
        studentsAdapter.submitList(filteredStudentsList)
    }
}
