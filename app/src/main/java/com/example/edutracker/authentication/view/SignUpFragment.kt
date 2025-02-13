package com.example.edutracker.authentication.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.edutracker.utilities.Constants
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentSignUpBinding
import com.example.edutracker.dataclasses.*
import com.example.edutracker.utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child(Constants.DATABASE_NAME)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.signUpButton.setOnClickListener {
            if (checkConnectivity(requireContext())) {
                val signupUserName = binding.signupUserNameET.text.toString()
                val signupEmail = binding.SignUpEmailET.text.toString()
                val signupSubject = binding.signupSubjectET.text.toString()
                val signupPhone = binding.signupPhoneNumberET.text.toString()
                val signupUserPassword = binding.signUpPasswordET.text.toString()
                val signupUserConfirmPassword = binding.signupConfirmPasswordET.text.toString()
                if (signupUserName.isNotEmpty() && signupEmail.isNotEmpty() && signupSubject.isNotEmpty() && signupPhone.isNotEmpty() && signupUserPassword.isNotEmpty() && signupUserConfirmPassword.isNotEmpty()) {
                    if (isValidEmail(signupEmail)) {
                        if (checkEgyptianPhoneNumber(signupPhone)) {
                            if (signupUserPassword.length < 6 || signupUserPassword.length > 20) {
                                Toast.makeText(requireContext(), getString(R.string.passwordLength), Toast.LENGTH_LONG).show()
                            } else {
                                if (checkPasswordMatch(signupUserPassword, signupUserConfirmPassword))
                                {
                                    FirebaseAuth.getInstance()
                                        .fetchSignInMethodsForEmail(signupEmail)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val signInMethods = task.result?.signInMethods
                                                if (signInMethods != null && signInMethods.isNotEmpty()) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        getString(R.string.signupExists),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    firebaseAuth.createUserWithEmailAndPassword(
                                                        signupEmail,
                                                        signupUserPassword
                                                    )
                                                        .addOnCompleteListener { createTask ->
                                                            if (createTask.isSuccessful) {
                                                                val user = createTask.result?.user
                                                                val userId = user?.uid
                                                                val teacher = Teacher(userId!!, signupUserName, signupPhone, signupUserPassword, signupEmail, signupSubject)

                                                                createTeacherNode(teacher)
                                                                MySharedPreferences.getInstance(requireContext()).saveTeacherID(userId)

                                                                Toast.makeText(requireContext(), getString(R.string.signupSuccessfully), Toast.LENGTH_SHORT).show()
                                                                Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_loginFragment)
                                                            } else {
                                                                Toast.makeText(requireContext(), getString(R.string.signupCancel), Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                }
                                            } else {
                                                // Error occurred while checking email existence
                                                Toast.makeText(requireContext(), "Try again plz", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.passwordNotMatch), Toast.LENGTH_SHORT).show()
                                }

                            }
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.emailNotValid), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
            }
        }
        binding.SignUpLoginTV.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_loginFragment)
        }

    }


    private fun createTeacherNode(teacher: Teacher) {
        val year1 = YearNode( "الفصل الدراسي الأول ٢٠٢٣-٢٠٢٤")
        val year2 = YearNode("الفصل الدراسي الثاني ٢٠٢٣-٢٠٢٤")
        val year3 = YearNode("الفصل الدراسي الأول ٢٠٢٤-٢٠٢٥")
        val year4 = YearNode("الفصل الدراسي الثاني ٢٠٢٤-٢٠٢٥")
        val year5 = YearNode("الفصل الدراسي الأول ٢٠٢٥-٢٠٢٦")
        val year6 = YearNode("الفصل الدراسي الثاني ٢٠٢٥-٢٠٢٦")
        val year7 = YearNode("الفصل الدراسي الأول ٢٠٢٦-٢٠٢٧")
        val year8 = YearNode("الفصل الدراسي الثاني ٢٠٢٦-٢٠٢٧")
        val year9 = YearNode("الفصل الدراسي الأول ٢٠٢٧-٢٠٢٨")
        val year10 = YearNode("الفصل الدراسي الثاني ٢٠٢٧-٢٠٢٨")
        val year11 = YearNode("الفصل الدراسي الأول ٢٠٢٨-٢٠٢٩")
        val year12 = YearNode("الفصل الدراسي الثاني ٢٠٢٨-٢٠٢٩")
        val year13 = YearNode("الفصل الدراسي الأول ٢٠٢٩-٢٠٣٠")
        val year14 = YearNode("الفصل الدراسي الثاني ٢٠٢٩-٢٠٣٠")
        val year15 = YearNode("الفصل الدراسي الأول ٢٠٣٠-٢٠٣١")
        val year16 = YearNode("الفصل الدراسي الثاني ٢٠٣٠-٢٠٣١")
        val year17 = YearNode("الفصل الدراسي الأول ٢٠٣١-٢٠٣٢")
        val year18 = YearNode("الفصل الدراسي الثاني ٢٠٣١-٢٠٣٢")
        val year19 = YearNode("الفصل الدراسي الأول ٢٠٣٢-٢٠٣٣")
        val year20 = YearNode("الفصل الدراسي الثاني ٢٠٣٢-٢٠٣٣")
        val list = listOf<YearNode>(year1,year2,year3,year4,year5,year6,year7,year8,year9,year10,year11,year12,year13,year14,year15,year16,year17,year18,year19,year20)
        databaseReference.child("Teachers").child(teacher.id).setValue(teacher)
        for (element in list){
            databaseReference.child("Teachers").child(teacher.id).child(element.id).setValue(element)
        }
    }

   /* fun addData(teacher_id: String) {
        val database = FirebaseDatabase.getInstance()
        val studentsRef = firebaseDatabase.getReference("Teachers")
*//*        val assistantsRef = database.getReference("Assistants")
        val groupsRef = database.getReference("Groups")
        val lessonsRef = database.getReference("Lessons")
        val studentStatusRef = database.getReference("StudentStatus")*//*

        // Add data to other nodes
        val student = Student(
            teacherId = "teacher1",
            id = "student1",
            name = "Alice Smith",
            password = "password",
            phoneNumber = "9876543210",
            parentId = "parent1",
            parentPassword = "parent_password",
            parentPhone = "9876543210",
            grade = "5",
            groupId = "group1"
        )
        databaseReference.child("Teachers").child(teacher_id).child("Students").child(student.id)
            .setValue(student)
        val student2 = Student(
            teacherId = "teacher1",
            id = "student2",
            name = "Alice Smith",
            password = "password",
            phoneNumber = "9876543210",
            parentId = "parent1",
            parentPassword = "parent_password",
            parentPhone = "9876543210",
            grade = "5",
            groupId = "group1"
        )
        databaseReference.child("Teachers").child(teacher_id).child("Students").child(student2.id)
            .setValue(student2)

        *//*
            val assistant = Assistant(
                id = "assistant1",
                name = "Bob Johnson",
                email = "bob.johnson@example.com",
                phone = "5555555555",
                password = "password",
                teacherId = "teacher1"
            )
            assistantsRef.child(assistant.id).setValue(assistant)

            val group = Group(
                teacherId = "teacher1",
                name = "Group A",
                id = "group1",
                gradeLevel = "5"
            )
            groupsRef.child(group.teacherId).child(group.id).setValue(group)

            val lesson = Lesson(
                id = "lesson1",
                groupId = "group1",
                teacherId = "teacher1",
                date = "2023-08-11",
                time = "10:00 AM"
            )
            lessonsRef.child(lesson.id).setValue(lesson)

            val studentStatus = StudentStatus(
                studentId = "student1",
                lessonId = "lesson1",
                status = "present"
            )
            studentStatusRef.child(studentStatus.studentId).child(studentStatus.lessonId).setValue(studentStatus)
     *//*
    }*/
}