package com.example.edutracker.authentication.view

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
import androidx.navigation.Navigation
import com.example.edutracker.utilities.Constants
import com.example.edutracker.teacher.MainActivity
import com.example.edutracker.R
import com.example.edutracker.authentication.viewmodel.AuthenticationViewModel
import com.example.edutracker.authentication.viewmodel.AuthenticationViewModelFactory
import com.example.edutracker.databinding.FragmentLoginBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Parent
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.student.StudentActivity
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var authenticationViewModelFactory: AuthenticationViewModelFactory

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var userType: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authenticationViewModelFactory =
            AuthenticationViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        authenticationViewModel = ViewModelProvider(
            this,
            authenticationViewModelFactory
        )[AuthenticationViewModel::class.java]

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child(Constants.DATABASE_NAME)

        binding.loginTeacherSignUp.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.radioButtonTeacher.id -> {
                    userType = Constants.TEACHER
                }
                binding.radioButtonAssistant.id -> {
                    userType = Constants.ASSISTANT
                }
                binding.radioButtonStudent.id -> {
                    userType = Constants.STUDENT
                }
                binding.radioButtonParent.id -> {
                    userType = Constants.PARENT
                }
            }
        }
        binding.loginbutton.setOnClickListener {
            val email = binding.loginEmailET.text.toString()
            val password = binding.loginPasswordET.text.toString()
            if (checkConnectivity(requireContext())) {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (userType != null) {
                        if (userType == Constants.TEACHER) {
                            teacherSignIn(email, password)
                        } else if (userType == Constants.ASSISTANT) {
                            assistantSignIn(email, password)
                        }else if (userType==Constants.STUDENT){
                            studentSignIn(email, password)
                        }else if (userType==Constants.PARENT){
                            parentSignIn(email, password)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.signinUserType),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.fillAllFields),
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun teacherSignIn(email: String, password: String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dialog.dismiss()
                    MySharedPreferences.getInstance(requireContext()).saveUserType(userType!!)
                    MySharedPreferences.getInstance(requireContext()).saveIsUserLoggedIn(true)
                    val user = firebaseAuth.currentUser
                    MySharedPreferences.getInstance(requireContext()).saveTeacherID(user!!.uid)
                    MySharedPreferences.getInstance(requireContext()).saveIsUserLoggedIn(true)
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    dialog.dismiss()
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.signinNotExists),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.signinWrongPassword),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is FirebaseAuthException -> {
                            Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                        is CancellationException -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.signInCancel),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.signinFailed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    private fun assistantSignIn(email: String, password: String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        lifecycleScope.launch {
            authenticationViewModel.assistantSignIn(
                email.replace(".", ","),
                password,
                requireContext()
            ) { result ->
                if (result.second) {
                            dialog.dismiss()
                            val assistant = result.first as Assistant
                            MySharedPreferences.getInstance(requireContext())
                                .saveIsUserLoggedIn(true)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserType(userType!!)
                            MySharedPreferences.getInstance(requireContext())
                                .saveTeacherID(assistant.teacherId)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserName(assistant.name)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserPassword(assistant.password)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserPhone(assistant.phone)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserEmail(assistant.email)
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        } else {
                            dialog.dismiss()
                            val error = result.first as String
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()

                    }
            }
        }

    }


    private fun studentSignIn(email: String, password: String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        lifecycleScope.launch {
            authenticationViewModel.studentSignIn(
                email.replace(".", ","),
                password,
                requireContext()
            ){ result ->
                        if (result.second) {
                            dialog.dismiss()
                            val student = result.first as Student
                            MySharedPreferences.getInstance(requireContext())
                                .saveIsUserLoggedIn(true)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserType(userType!!)
                            MySharedPreferences.getInstance(requireContext())
                                .saveTeacherID(student.teacherId)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserName(student.name)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserPassword(student.password)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserPhone(student.phoneNumber)
                            MySharedPreferences.getInstance(requireContext())
                                .saveStudentID(student.email)
                            MySharedPreferences.getInstance(requireContext())
                                .saveUserEmail(student.email)
                            MySharedPreferences.getInstance(requireContext())
                                .saveSemester(student.activeSemester)
                            MySharedPreferences.getInstance(requireContext())
                                .saveStudentGradeLevel(student.activeGradeLevel)

                            MySharedPreferences.getInstance(requireContext())
                                .saveStudentGroupID(student.activeGroupId)
                            val intent = Intent(requireActivity(), StudentActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        } else {
                            dialog.dismiss()
                            val error = result.first as String
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        }
                    }



            }
        }


    private fun parentSignIn(email: String, password: String) {
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
            try {
                authenticationViewModel.parentSignIn(
                    email.replace(".", ","),
                    password,
                    requireContext()
                ){ result ->
                    if (result.second) {
                        val parent = result.first as Parent
                        MySharedPreferences.getInstance(requireContext())
                            .saveIsUserLoggedIn(true)
                        MySharedPreferences.getInstance(requireContext())
                            .saveUserEmail((result.first as Parent).parentEmail)
                        MySharedPreferences.getInstance(requireContext())
                            .saveUserPhone((result.first as Parent).parentPhone)
                        MySharedPreferences.getInstance(requireContext())
                            .saveUserPassword((result.first as Parent).parentPassword)

                        lifecycleScope.launch {

                            authenticationViewModel.getStudentById(parent.studentId)
                            authenticationViewModel.getStudent.collect { result ->
                                when (result) {
                                    is FirebaseState.Loading -> {
                                        // Handle loading state if needed
                                    }
                                    is FirebaseState.Success -> {
                                        val student = result.data
                                        if (student != null) {
                                            dialog.dismiss()
                                            MySharedPreferences.getInstance(requireContext())
                                                .saveUserName(student.name)
                                            MySharedPreferences.getInstance(requireContext())
                                                .saveStudentID(student.email)
                                            MySharedPreferences.getInstance(requireContext())
                                                .saveSemester(student.activeSemester)
                                            MySharedPreferences.getInstance(requireContext())
                                                .saveStudentGradeLevel(student.activeGradeLevel)
                                            val intent = Intent(requireActivity(), StudentActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.signinNotExists),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    is FirebaseState.Failure -> {
                                        // Handle failure state if needed
                                    }
                                }
                            }
                        }
                    } else {
                        dialog.dismiss()
                        val error = result.first as String
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }



                }
            }catch (excepthion:Exception){
                Log.i("exception", "parentSignIn: $excepthion")
            }


    }

}