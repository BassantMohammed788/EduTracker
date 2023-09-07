package com.example.edutracker.teacher.students.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentAddParentBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Parent
import com.example.edutracker.models.Repository
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.example.edutracker.utilities.checkEgyptianPhoneNumber
import com.example.edutracker.utilities.isValidEmail

class AddParentFragment : Fragment() {
    private lateinit var binding:FragmentAddParentBinding
    private val args: AddParentFragmentArgs by navArgs()
    private var studentId = ""
    private var studentName=""
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private var teacherIdVar : String? = null


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
            val parentPhone = binding.parentPhoneNumberET.text.toString()
            val parentPassword = binding.parentPasswordET.text.toString()
            lateinit var parent :Parent
            val builder = AlertDialog.Builder(requireContext())
            val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
            builder.setView(loadingDialogB.root)
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
                try {
                    if (checkConnectivity(requireContext())) {
                        if (parentEmail.isNotEmpty() && parentPassword.isNotEmpty() && parentPhone.isNotEmpty()) {
                            if (isValidEmail(parentEmail)) {
                                parentEmail = parentEmail.replace(".", ",")
                                if (checkEgyptianPhoneNumber(parentPhone)) {
                                    parent = Parent(parentEmail, parentPassword, parentPhone, studentId, teacherIdVar!!)
                                     studentsViewModel.addParent(parent, teacherIdVar!!, studentId){ result->
                                         if (result){
                                             dialog.dismiss()
                                             Toast.makeText(requireContext(), getString(R.string.added_successfully), Toast.LENGTH_SHORT).show()
                                             Navigation.findNavController(requireView()).apply {
                                                 popBackStack()
                                                 popBackStack()
                                             }
                                         }else{
                                             dialog.dismiss()
                                             Toast.makeText(requireContext(), getString(R.string.The_email_already_exists), Toast.LENGTH_SHORT).show()
                                         }}

                                } else {
                                    dialog.dismiss()
                                    Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                                }
                            } else {

                                dialog.dismiss()
                                Toast.makeText(requireContext(), getString(R.string.emailNotValid), Toast.LENGTH_SHORT).show()
                            }
                        } else {

                            dialog.dismiss()
                            Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        dialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.network_lost_title),
                            Toast.LENGTH_SHORT
                        ).show()
                    }}catch (e:Exception){
                        dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    }


        }

    }


}