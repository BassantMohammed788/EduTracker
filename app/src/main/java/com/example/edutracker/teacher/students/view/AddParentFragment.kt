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
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentAddParentBinding
import com.example.edutracker.databinding.FragmentAddStudentBinding
import com.example.edutracker.dataclasses.Parent
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.example.edutracker.utilities.checkEgyptianPhoneNumber
import com.example.edutracker.utilities.isValidEmail
import kotlinx.coroutines.launch

class AddParentFragment : Fragment() {
    private lateinit var binding:FragmentAddParentBinding
    private val args: AddParentFragmentArgs by navArgs()
    private var studentId = ""
    private var studentName=""
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private var teacherIdVar : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()
        studentId=args.studentId
        studentName=args.studentName
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack()
            }
        }

        binding.AddButton.setOnClickListener {
            var parentEmail = binding.parentEmailET.text.toString()
            val parentPhone= binding.parentPhoneNumberET.text.toString()
            val parentPassword= binding.parentPasswordET.text.toString()
            if (checkConnectivity(requireContext())){
            if (parentEmail.isNotEmpty()&&parentPassword.isNotEmpty()&&parentPhone.isNotEmpty()) {
                if (isValidEmail(parentEmail)) {
                    parentEmail=parentEmail.replace(".", ",")
                    if (checkEgyptianPhoneNumber(parentPhone)) {
                        val parent = Parent(parentEmail,parentPassword,parentPhone,studentId,teacherIdVar!!)
                        lifecycleScope.launch {
                            studentsViewModel.addParent(parent,teacherIdVar!!,studentId)
                            studentsViewModel.addParent.collect { result ->
                                when (result) {
                                    is FirebaseState.Loading -> {
                                        Log.i("TAG", "onViewCreated: loading")
                                    }
                                    is FirebaseState.Success -> {
                                        if (result.data) {
                                            Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show()
                                            Navigation.findNavController(requireView()).apply {
                                                popBackStack()
                                                popBackStack()// Clear the back stack up to teacherAllAssistantFragment
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


}