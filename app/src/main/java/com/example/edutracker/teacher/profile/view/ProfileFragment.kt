package com.example.edutracker.teacher.profile.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.edutracker.databinding.FragmentProfileBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.assistantdata.viewmodel.AssistantDataViewModel
import com.example.edutracker.teacher.assistantdata.viewmodel.AssistantDataViewModelFactory
import com.example.edutracker.teacher.profile.viewmodel.ProfileViewModel
import com.example.edutracker.teacher.profile.viewmodel.ProfileViewModelFactory
import com.example.edutracker.utilities.*
import com.google.firebase.auth.*
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var profileViewModelFactory: ProfileViewModelFactory

    private lateinit var viewModel: AssistantDataViewModel
    private lateinit var viewModelFactory: AssistantDataViewModelFactory

    private var semesterVar: String? = null
    private var teacherNameVar: String? = null
    private var teacherEmailVar: String? = null
    private var teacherPhoneVar: String? = null
    private var teacherSubjectVar: String? = null
    private var teacherIdVar: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModelFactory = ProfileViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        profileViewModel = ViewModelProvider(this, profileViewModelFactory)[ProfileViewModel::class.java]

        viewModelFactory =
            AssistantDataViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[AssistantDataViewModel::class.java]

        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!

        if (MySharedPreferences.getInstance(requireContext()).getSemester()!=null){

            semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!
            if (checkConnectivity(requireContext())) {
                lifecycleScope.launch {
                    if (semesterVar!=null) {
                        profileViewModel.getGradesAndGroupsCounts(semesterVar!!, teacherIdVar!!)
                        profileViewModel.getGradeAndGroupCounts.collect { result ->
                            when (result) {
                                is FirebaseState.Success -> {
                                    binding.gradeLevelsCount.text = result.data.first.toString()
                                    binding.groupsCount.text = result.data.second.toString()
                                }
                                else -> {}
                            }}
                    }

                }
                lifecycleScope.launch {
                    if (semesterVar!=null) {
                        profileViewModel.getAllStudents(teacherIdVar!!, semesterVar!!)
                        profileViewModel.getStudent.collect { result ->
                            when (result) {
                                is FirebaseState.Success -> {
                                    binding.studentsCount.text = result.data.size.toString()
                                }
                                else -> {}
                            }}
                    }}
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.network_lost_title),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (MySharedPreferences.getInstance(requireContext()).getUserType()==Constants.TEACHER){
        if (MySharedPreferences.getInstance(requireContext()).getTeacherEmail()==null){
            lifecycleScope.launch {
                if (checkConnectivity(requireContext())){
                    profileViewModel.getTeacherById(teacherIdVar!!)
                    profileViewModel.getTeacher.collect{ result->
                        when(result){
                            is FirebaseState.Success->{
                                binding.emailTv.text=result.data.email
                                binding.name.text=result.data.name
                                binding.phoneTv.text=result.data.phone
                                binding.subjectTv.text=result.data.subject
                                MySharedPreferences.getInstance(requireContext()).saveTeacherName(result.data.name)
                                MySharedPreferences.getInstance(requireContext()).saveTeacherEmail(result.data.email)
                                MySharedPreferences.getInstance(requireContext()).saveTeacherPhone(result.data.phone)
                                MySharedPreferences.getInstance(requireContext()).saveTeacherSubject(result.data.subject)
                            }
                            else->{
                            }
                        }
                    }

                }else{
                    Toast.makeText(requireContext(),getString(R.string.network_lost_title),Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            teacherNameVar = MySharedPreferences.getInstance(requireContext()).getTeacherName()
            teacherEmailVar = MySharedPreferences.getInstance(requireContext()).getTeacherEmail()
            teacherPhoneVar = MySharedPreferences.getInstance(requireContext()).getTeacherPhone()
            teacherSubjectVar = MySharedPreferences.getInstance(requireContext()).getTeacherSubject()
            binding.emailTv.text=teacherEmailVar?.replace(",", ".")
            binding.subjectTv.text=teacherSubjectVar
            binding.subjectTv.visibility=View.GONE
            binding.phoneTv.text=teacherPhoneVar
            binding.name.text=teacherNameVar
        }
        }else{
            teacherNameVar = MySharedPreferences.getInstance(requireContext()).getUserName()
            teacherEmailVar = MySharedPreferences.getInstance(requireContext()).getUserEmail()
            teacherPhoneVar = MySharedPreferences.getInstance(requireContext()).getUserPhone()
            binding.subjectTv.visibility=View.GONE
            binding.emailTv.text= teacherEmailVar?.replace(",", ".")
            binding.phoneTv.text =teacherPhoneVar
            binding.name.text=teacherNameVar
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
            MySharedPreferences.getInstance(requireContext()).saveTeacherName(null)
            MySharedPreferences.getInstance(requireContext()).saveTeacherEmail(null)
            MySharedPreferences.getInstance(requireContext()).saveTeacherPhone(null)
            MySharedPreferences.getInstance(requireContext()).saveTeacherSubject(null)
            MySharedPreferences.getInstance(requireContext()).saveUserEmail(null)
            MySharedPreferences.getInstance(requireContext()).saveUserPassword(null)
            MySharedPreferences.getInstance(requireContext()).saveUserPhone(null)
            MySharedPreferences.getInstance(requireContext()).saveUserName(null)
            MySharedPreferences.getInstance(requireContext()).saveSemester(null)

            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()}
            alertDialog.dialogNoBtn.setOnClickListener {
                dialog.dismiss()
            }
        }


        binding.changePasswordButton.setOnClickListener {
            displayChangePasswordDialog()
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
                if (new == confirm) {
                   if (checkConnectivity(requireContext())) {
                       if (MySharedPreferences.getInstance(requireContext()).getUserType()==Constants.TEACHER){
                           changeTeacherPassword(current, new) { isSuccess ->
                            if (isSuccess) {
                                dialog.dismiss()
                            }
                        }
                       }
                    else if (MySharedPreferences.getInstance(requireContext()).getUserType()==Constants.ASSISTANT) {
                         changeAssistantPassword(new)
                           dialog.dismiss()
                    }
                   }
                     else {
                        Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.passwordNotMatch), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_LONG).show()
            }
        }
        changePasswordDialog.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun changeTeacherPassword(currentPassword: String, newPassword: String, onComplete: (Boolean) -> Unit) {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        user?.let {
            val credential: AuthCredential =  EmailAuthProvider.getCredential(user.email!!, currentPassword)
            val builder = AlertDialog.Builder(requireContext())
            val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
            builder.setView(loadingDialogB.root)
            val dialog = builder.create()
            dialog.setCancelable(false)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        if (currentPassword == newPassword) {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), getString(R.string.current_password_is_the_same_as_new), Toast.LENGTH_LONG).show()
                            onComplete(false)
                        } else {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { passwordTask ->
                                    dialog.dismiss()
                                    if (passwordTask.isSuccessful) {
                                        Toast.makeText(requireContext(), getString(R.string.updated_successfully), Toast.LENGTH_LONG).show()
                                        onComplete(true)
                                    } else {
                                        val errorMessage = passwordTask.exception?.message
                                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                                        onComplete(false)
                                    }
                                }
                        }
                    } else {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), getString(R.string.incorrect_current_password), Toast.LENGTH_LONG).show()
                        onComplete(false)
                    }
                }
        }
    }

    private fun changeAssistantPassword(password:String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
      lifecycleScope.launch {
                val assistant = Assistant(teacherNameVar!!, teacherEmailVar!!, teacherPhoneVar!!, password, teacherIdVar!!)
                viewModel.updateAssistant(assistant)
                viewModel.updateAssistant.collect {
                    when (it) {
                        is FirebaseState.Loading -> {
                            dialog.show()
                            Log.i("TAG", "onViewCreated: loading update")
                        }
                        is FirebaseState.Success -> {
                            dialog.dismiss()
                            if (it.data) {
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
    }
}