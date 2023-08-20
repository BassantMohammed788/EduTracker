package com.example.edutracker.authentication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.edutracker.utilities.Constants
import com.example.edutracker.MainActivity
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentLoginBinding
import com.example.edutracker.dataclasses.Teacher
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.*
import kotlinx.coroutines.CancellationException

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private  var userType :String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child(Constants.DATABASE_NAME)

        binding.loginTeacherSignUp.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
               binding.radioButtonTeacher.id-> {
                    userType= Constants.TEACHER
                }
                binding.radioButtonAssistant.id -> {
                    userType= Constants.ASSISTANT
                }
                binding.radioButtonStudent.id -> {
                    userType= Constants.STUDENT
                }
               binding.radioButtonParent.id -> {
                   userType= Constants.PARENT
                }
            }
        }
        binding.loginbutton.setOnClickListener {
            val email = binding.loginEmailET.text.toString()
            val password = binding.loginPasswordET.text.toString()
            if (checkConnectivity(requireContext())){
                if (email.isNotEmpty()&&password.isNotEmpty()){
                    if (userType!=null){
                            MySharedPreferences.getInstance(requireContext()).saveUserType(userType!!)
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Sign-in successful
                                        val user = firebaseAuth.currentUser
                                        Toast.makeText(requireContext(), getString(R.string.signinSuccessfully), Toast.LENGTH_SHORT).show()
                                        // Do something with the user
                                        MySharedPreferences.getInstance(requireContext()).saveTeacherID(user!!.uid)
                                        val intent = Intent(requireActivity(), MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                    } else {
                                        val exception = task.exception
                                        if (exception is FirebaseAuthInvalidUserException) {
                                            // User doesn't exist
                                            Toast.makeText(requireContext(),  getString(R.string.signinNotExists), Toast.LENGTH_SHORT).show()
                                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                                            // Wrong password
                                            Toast.makeText(requireContext(),   getString(R.string.signinWrongPassword), Toast.LENGTH_SHORT).show()
                                        } else if (exception is FirebaseAuthException) {
                                            // Other FirebaseAuthException occurred
                                            Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                                        } else if (exception is CancellationException) {
                                            // Sign-in canceled
                                            Toast.makeText(requireContext(), getString(R.string.signInCancel), Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Other exceptions occurred
                                            Toast.makeText(requireContext(), "Sign-in failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                    }else{
                        Toast.makeText(requireContext(), getString(R.string.signinUserType), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(
                    requireContext(),
                    getString(R.string.network_lost_title),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun signInTeacher(
        email: String,
        password: String
    ) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.getValue(Teacher::class.java)
                            if (userData != null && userData.password == password) {
                                Toast.makeText(
                                    requireContext(),getString(R.string.signinSuccessfully),
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.signinWrongPassword),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.signinNotExists),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.signupCancel),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    fun create(){
        val studentId = "student1"
        val yearRef: DatabaseReference = databaseReference.child(Constants.YEAR_NODE)
        yearRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var studentExists = false
                if (dataSnapshot.exists()) {
                    for (yearSnapshot in dataSnapshot.children) {
                        val yearId = yearSnapshot.key
                        if (yearSnapshot.child("Students").hasChild(studentId)) {
                            studentExists = true
                            println("Student found in Year ID: $yearId")
                            break
                        }
                    }
                }
                if (!studentExists) {
                    println("Student not found in any year")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read year data: ${error.message}")
            }
        })

    }

}