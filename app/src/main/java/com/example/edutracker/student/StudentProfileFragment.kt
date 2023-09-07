package com.example.edutracker.student

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.edutracker.R
import com.example.edutracker.authentication.view.AuthenticationActivity
import com.example.edutracker.databinding.AlertDialogBinding
import com.example.edutracker.databinding.ChangePasswordDialogBinding
import com.example.edutracker.databinding.FragmentStudentProfileBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Parent
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.Constants
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class StudentProfileFragment : Fragment() {
    private lateinit var binding: FragmentStudentProfileBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentsViewModelFactory =
            StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel =
            ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        if (MySharedPreferences.getInstance(requireContext()).getUserType() == Constants.PARENT) {
            binding.name.text = "${getString(R.string.the_student)} : ${
                MySharedPreferences.getInstance(requireContext()).getUserName()
            }"
        } else {
            binding.name.text = MySharedPreferences.getInstance(requireContext()).getUserName()
        }
        binding.phoneTv.text = MySharedPreferences.getInstance(requireContext()).getUserPhone()
        binding.emailTv.text =
            MySharedPreferences.getInstance(requireContext()).getUserEmail()?.replace(",", ".")

        binding.changePasswordButton.setOnClickListener {
            displayChangePasswordDialog()
        }


        binding.logoutButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val alertDialog = AlertDialogBinding.inflate(layoutInflater)
            builder.setView(alertDialog.root)
            val dialog = builder.create()
            dialog.show()
            dialog.setCancelable(false)
            alertDialog.dialogMessage.text=getString(R.string.are_you_sure_you_want_to_logout)
            alertDialog.dialogYesBtn.setOnClickListener{
                MySharedPreferences.getInstance(requireContext()).saveIsUserLoggedIn(false)
                MySharedPreferences.getInstance(requireContext()).saveStudentID(null)
                MySharedPreferences.getInstance(requireContext()).saveUserEmail(null)
                MySharedPreferences.getInstance(requireContext()).saveUserPassword(null)
                MySharedPreferences.getInstance(requireContext()).saveUserPhone(null)
                MySharedPreferences.getInstance(requireContext()).saveUserName(null)
                MySharedPreferences.getInstance(requireContext()).saveStudentGroupID(null)
                MySharedPreferences.getInstance(requireContext()).saveStudentGradeLevel(null)
                MySharedPreferences.getInstance(requireContext()).saveSemester(null)
                val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            alertDialog.dialogNoBtn.setOnClickListener {
                dialog.dismiss()
            }


        }
    }

    private fun displayChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val changePasswordDialog = ChangePasswordDialogBinding.inflate(layoutInflater)
        builder.setView(changePasswordDialog.root)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        changePasswordDialog.changePasswordButton.setOnClickListener {
            val current = changePasswordDialog.currentPasswordET.text.toString()
            val new = changePasswordDialog.newPasswordET.text.toString()
            val confirm = changePasswordDialog.ConfirmPasswordET.text.toString()
            if (current.isNotEmpty() && new.isNotEmpty() && confirm.isNotEmpty()) {
               if (current==MySharedPreferences.getInstance(requireContext()).getUserPassword()){
                   if (new == confirm) {
                       if (checkConnectivity(requireContext())) {
                           if (MySharedPreferences.getInstance(requireContext())
                                   .getUserType() == Constants.STUDENT
                           ) {
                               changeStudentPassword(new)
                               dialog.dismiss()
                           } else if (MySharedPreferences.getInstance(requireContext())
                                   .getUserType() == Constants.PARENT
                           ) {
                               updateParent(new)
                               dialog.dismiss()
                           }
                       } else {
                           Toast.makeText(
                               requireContext(),
                               getString(R.string.network_lost_title),
                               Toast.LENGTH_LONG
                           ).show()
                       }
                   }
                   else {
                       Toast.makeText(requireContext(), getString(R.string.passwordNotMatch), Toast.LENGTH_LONG).show()
                   }
               }else{
                   Toast.makeText(requireContext(), getString(R.string.incorrect_current_password), Toast.LENGTH_LONG).show()
               }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.fillAllFields),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        changePasswordDialog.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }


    private fun changeStudentPassword(password: String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        val shared = MySharedPreferences.getInstance(requireContext())
        val updatedStudent = Student(
            shared.getStudentID()!!,
            shared.getUserName()!!,
            shared.getTeacherID()!!,
            password,
            shared.getUserPhone()!!,
            shared.getStudentGradeLevel()!!,
            shared.getStudentGroupID()!!,
            shared.getSemester()!!
        )
        if (checkConnectivity(requireContext())) {
            lifecycleScope.launch {
                studentsViewModel.updateStudent(updatedStudent)
                studentsViewModel.updateStudent.collect { result ->
                    when (result) {
                        is FirebaseState.Loading -> {
                            dialog.show()
                        }
                        is FirebaseState.Success -> {
                            dialog.dismiss()
                            if (result.data) {
                                MySharedPreferences.getInstance(requireContext()).saveUserPassword(password)
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.updated_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.network_lost_title),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is FirebaseState.Failure -> {
                            dialog.dismiss()
                        }
                    }
                }
            }
        } else {
            dialog.dismiss()
            Toast.makeText(
                requireContext(),
                getString(R.string.network_lost_title),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun updateParent(password: String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        if (checkConnectivity(requireContext())) {
            lifecycleScope.launch {
                val shared =MySharedPreferences.getInstance(requireContext())
                val updatedParent = Parent(shared.getUserEmail()!!, password, shared.getUserPhone()!!, shared.getStudentID()!!, shared.getTeacherID()!!)
                    studentsViewModel.updateParent(updatedParent)
                    studentsViewModel.updateParent.collect {
                        when (it) {
                            is FirebaseState.Loading -> {
                              dialog.show()
                            }
                            is FirebaseState.Success -> {
                                dialog.dismiss()
                                if (it.data) {
                                    MySharedPreferences.getInstance(requireContext()).saveUserPassword(password)
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.updated_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            else -> {
                                dialog.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.network_lost_title),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
    }
}