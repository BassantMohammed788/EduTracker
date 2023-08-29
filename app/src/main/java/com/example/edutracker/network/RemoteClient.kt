package com.example.edutracker.network

import com.example.edutracker.utilities.Constants
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import com.example.edutracker.dataclasses.Student
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RemoteClient private constructor() : RemoteInterface {
    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_NAME)
    }

    companion object {
        private var instance: RemoteClient? = null
        fun getInstance(): RemoteClient {
            return instance ?: synchronized(this) {
                val temp = RemoteClient()
                instance = temp
                temp
            }
        }
    }

    override fun addAssistant(assistant: Assistant, teacher_id: String): Flow<Boolean> =
        callbackFlow {
            val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
                .child(Constants.ASSISTANT_NODE).child(assistant.email)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val emailExists = dataSnapshot.exists()
                    if (emailExists) {
                        trySend(false)
                    } else {
                        assistantRef.setValue(assistant)
                            .addOnSuccessListener {
                                trySend(true)
                            }
                            .addOnFailureListener { exception ->
                                close(exception)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            assistantRef.addListenerForSingleValueEvent(valueEventListener)

            awaitClose { assistantRef.removeEventListener(valueEventListener) }
        }

    override fun getAllAssistants(teacher_id: String): Flow<List<Assistant>> = callbackFlow {
        val assistantsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id).child(
            Constants.ASSISTANT_NODE
        )
        val assistantsList = mutableListOf<Assistant>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                assistantsList.clear()
                for (assistantSnapshot in dataSnapshot.children) {
                    val assistant = assistantSnapshot.getValue(Assistant::class.java)
                    assistant?.let { assistantsList.add(it) }
                }
                trySend(assistantsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        assistantsRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { assistantsRef.removeEventListener(valueEventListener) }
    }

    override fun getAssistantByEmail(teacher_id: String, email: String): Flow<Assistant> =
        callbackFlow {
            val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
                .child(Constants.ASSISTANT_NODE).child(email)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val assistant = dataSnapshot.getValue(Assistant::class.java)
                    if (assistant != null) {
                        trySend(assistant).isSuccess // Emit the assistant to the flow
                    } else {
                        // Handle the case when assistant is null
                        // You can close the flow or take appropriate action
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException()) // Close the flow with an error
                }
            }
            assistantRef.addValueEventListener(valueEventListener)

            // Remove the listener when the flow collector is cancelled
            awaitClose { assistantRef.removeEventListener(valueEventListener) }
        }

    override fun updateAssistantData(
        teacher_id: String,
        email: String,
        updatedAssistant: Assistant
    ): Flow<Assistant> = callbackFlow {
        val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(Constants.ASSISTANT_NODE).child(email)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                assistantRef.setValue(updatedAssistant)
                    .addOnSuccessListener {
                        trySend(updatedAssistant).isSuccess // Emit the updated assistant to the flow
                    }
                    .addOnFailureListener { exception ->
                        close(exception) // Close the flow with an error
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        assistantRef.addListenerForSingleValueEvent(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { assistantRef.removeEventListener(valueEventListener) }
    }

    override fun deleteAssistantByEmail(teacher_id: String, email: String) {
        val assistantRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(Constants.ASSISTANT_NODE).child(email)
        assistantRef.removeValue()
    }


    override fun addGroup(group: Group, teacher_id: String, semester: String): Flow<Boolean> =
        callbackFlow {
            val groupRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
                .child(semester).child(Constants.GRADE_NODE).child(group.gradeLevel).child(group.id)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val emailExists = dataSnapshot.exists()
                    if (emailExists) {
                        trySend(false)
                    } else {
                        groupRef.setValue(group)
                            .addOnSuccessListener {
                                trySend(true)
                            }
                            .addOnFailureListener { exception ->
                                close(exception)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            groupRef.addListenerForSingleValueEvent(valueEventListener)

            awaitClose { groupRef.removeEventListener(valueEventListener) }
        }

    override fun getAllGroups(
        semester: String,
        teacher_id: String,
        gradeLevel: String
    ): Flow<List<Group>> = callbackFlow {
        val groupsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(gradeLevel)
        val groupsList = mutableListOf<Group>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupsList.clear()
                for (assistantSnapshot in dataSnapshot.children) {
                    val assistant = assistantSnapshot.getValue(Group::class.java)
                    assistant?.let { groupsList.add(it) }
                }
                trySend(groupsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        groupsRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { groupsRef.removeEventListener(valueEventListener) }
    }

    override fun deleteGroup(
        semester: String,
        teacher_id: String,
        gradeLevel: String,
        group_id: String
    ) {
        val groupsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(gradeLevel).child(group_id)
        groupsRef.removeValue()
    }


    override fun getAllGrades(semester: String, teacherId: String): Flow<List<String>> =
        callbackFlow {
            val gradesRef = databaseReference.child(Constants.TEACHER_NODE).child(teacherId)
                .child(semester).child(Constants.GRADE_NODE)
            val gradeIdList = mutableListOf<String>()

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    gradeIdList.clear()
                    for (gradeSnapshot in dataSnapshot.children) {
                        val gradeId = gradeSnapshot.key // Retrieve the grade ID
                        gradeId?.let { gradeIdList.add(it) }
                    }
                    trySend(gradeIdList.toList()).isSuccess // Emit the updated list
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException()) // Close the flow with an error
                }
            }

            gradesRef.addValueEventListener(valueEventListener)

            // Remove the listener when the flow collector is cancelled
            awaitClose { gradesRef.removeEventListener(valueEventListener) }
        }

    override fun getAllMonths(
        semester: String,
        teacherId: String,
        grade_level: String,
        group_id: String
    ): Flow<List<String>> = callbackFlow {
        val gradesRef = databaseReference.child(Constants.TEACHER_NODE).child(teacherId)
            .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(group_id)
        val monthIdList = mutableListOf<String>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                monthIdList.clear()
                for (monthSnapshot in dataSnapshot.children) {
                    val gradeId = monthSnapshot.key // Retrieve the grade ID
                    gradeId?.let { monthIdList.add(it) }
                }
                trySend(monthIdList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        gradesRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { gradesRef.removeEventListener(valueEventListener) }
    }


    override fun addLesson(
        teacher_id: String,
        semester: String,
        grade_level: String,
        month: String,
        lesson: Lesson
    ): Flow<Boolean> = callbackFlow {
        val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(lesson.groupId)
            .child(month).child(Constants.LESSON_NODE).child(lesson.id)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lessonExists = dataSnapshot.exists()
                if (lessonExists) {
                    trySend(false)
                } else {
                    lessonRef.setValue(lesson)
                        .addOnSuccessListener {
                            trySend(true)
                        }
                        .addOnFailureListener { exception ->
                            close(exception)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        lessonRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { lessonRef.removeEventListener(valueEventListener) }
    }
 /* override fun addLesson(
      teacher_id: String,
      semester: String,
      grade_level: String,
      month: String,
      lesson: Lesson,
      studentList: List<Student> // List of student IDs
  ): Flow<Boolean> = callbackFlow {
      val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
          .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(lesson.groupId)
          .child(month).child(Constants.LESSON_NODE).child(lesson.id)

      val attendanceRef = databaseReference.child(Constants.STUDENT_NODE)

      val valueEventListener = object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
              val lessonExists = dataSnapshot.exists()
              if (lessonExists) {
                  trySend(false)
              } else {
                  // Add the lesson
                  lessonRef.setValue(lesson)
                      .addOnSuccessListener {
                          // Add attendance status for each student
                          studentList.forEach { studentId ->
                              val attendanceStatusRef = attendanceRef.child(studentId.email)
                                  .child(Constants.SEMESTER_NODE).child(semester).child(Constants.GRADE_NODE).child(grade_level)
                                  .child(Constants.GROUP_NODE).child(lesson.groupId)
                                  .child(month).child(lesson.id).child("attendanceStatus")

                              attendanceStatusRef.setValue("No Attendence Yet")
                                  .addOnSuccessListener {
                                      // Attendance status added for the student
                                  }
                                  .addOnFailureListener { exception ->
                                      close(exception)
                                      return@addOnFailureListener
                                  }
                          }
                          trySend(true)
                      }
                      .addOnFailureListener { exception ->
                          close(exception)
                      }
              }
          }

          override fun onCancelled(error: DatabaseError) {
              close(error.toException())
          }
      }
      lessonRef.addListenerForSingleValueEvent(valueEventListener)

      awaitClose { lessonRef.removeEventListener(valueEventListener) }
  }*/

    override fun getAllLessons(
        teacher_id: String,
        semester: String,
        grade_level: String,
        group_id: String,
        month: String
    ): Flow<List<Lesson>> = callbackFlow {
        val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(group_id)
            .child(month).child(Constants.LESSON_NODE)

        val lessonsList = mutableListOf<Lesson>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                lessonsList.clear()
                for (assistantSnapshot in dataSnapshot.children) {
                    val assistant = assistantSnapshot.getValue(Lesson::class.java)
                    assistant?.let { lessonsList.add(it) }
                }
                trySend(lessonsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        lessonRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { lessonRef.removeEventListener(valueEventListener) }
    }

    override fun deleteLesson(
        semester: String,
        teacher_id: String,
        gradeLevel: String,
        group_id: String,
        month: String,
        lesson_id: String
    ) {
        val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(gradeLevel).child(group_id)
            .child(month).child(Constants.LESSON_NODE).child(lesson_id)
        lessonRef.removeValue()
    }

    override fun addStudent(
        student: Student,
        teacher_id: String,
        semester: String,
        grade_level: String,
        group_id: String
    ): Flow<Boolean> = callbackFlow {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(student.email)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentExists = dataSnapshot.exists()
                if (studentExists) {
                    trySend(false)
                } else {
                    // Add the student
                    studentRef.setValue(student)
                        .addOnSuccessListener {
                            // Student added successfully, now add data to the specific path
                            val dataRef = studentRef.child(Constants.SEMESTER_NODE).child(semester)
                                .child(Constants.GRADE_NODE).child(grade_level)
                                .child(Constants.GROUP_NODE).child(group_id).child("Status")
                             dataRef.setValue("active") // Replace `yourData` with the data you want to add
                            trySend(true)
                        }
                        .addOnFailureListener { exception ->
                            close(exception)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        studentRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentRef.removeEventListener(valueEventListener) }
    }

    override fun getAllStudents(teacher_id: String): Flow<List<Student>> = callbackFlow {
        val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

        val studentsList = mutableListOf<Student>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                studentsList.clear()
                for (studentSnapshot in dataSnapshot.children) {
                    val student = studentSnapshot.getValue(Student::class.java)
                    student?.let {
                        if (it.teacherId == teacher_id) {
                            studentsList.add(it)
                        }
                    }
                }
                trySend(studentsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        studentsRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }
    override fun getAllStudentsOfGroup(teacher_id: String, group_id: String): Flow<List<Student>> = callbackFlow {
        val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

        val studentsList = mutableListOf<Student>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                studentsList.clear()
                for (studentSnapshot in dataSnapshot.children) {
                    val student = studentSnapshot.getValue(Student::class.java)
                    student?.let {
                        if (it.teacherId == teacher_id && it.activeGroupId == group_id) {
                            studentsList.add(it)
                        }
                    }
                }
                trySend(studentsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        studentsRef.addValueEventListener(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }
    override fun getStudentAttendanceForLesson(
        semester: String,
        gradeLevel: String,
        groupId: String,
        lessonId: String,
        month: String,
        studentList: List<Student>
    ): Flow<List<Triple<String, String, String>>> = callbackFlow {
        val studentAttendanceList = mutableListOf<Triple<String, String, String>>()

        val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                studentAttendanceList.clear()
                for (student in studentList) {
                    val studentId = student.email
                    val studentSnapshot = dataSnapshot.child(studentId)
                    val attendanceStatus = studentSnapshot
                        .child(Constants.SEMESTER_NODE)
                        .child(semester)
                        .child(Constants.GRADE_NODE)
                        .child(gradeLevel)
                        .child(Constants.GROUP_NODE)
                        .child(groupId)
                        .child(month)
                        .child(lessonId)
                        .child("attendanceStatus")
                        .getValue(String::class.java) ?: "No attendance yet"
                    val studentName = student.name

                    if (attendanceStatus != null) {
                        studentAttendanceList.add(Triple(studentId, studentName, attendanceStatus))
                    } else {
                        studentAttendanceList.add(Triple(studentId, studentName, "No attendance yet"))
                    }
                }
                trySend(studentAttendanceList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an error
            }
        }

        studentsRef.addListenerForSingleValueEvent(valueEventListener)

        // Remove the listener when the flow collector is cancelled
        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }
    override fun addStudentAttendanceForLesson(
        semester: String,
        gradeLevel: String,
        groupId: String,
        lessonId: String,
        month: String,
        studentId: String,
        attendanceStatus: String
    ) {
        val studentRef = databaseReference
            .child(Constants.STUDENT_NODE)
            .child(studentId)
            .child(Constants.SEMESTER_NODE)
            .child(semester)
            .child(Constants.GRADE_NODE)
            .child(gradeLevel)
            .child(Constants.GROUP_NODE)
            .child(groupId)
            .child(month)
            .child(lessonId)
            .child("attendanceStatus")

        studentRef.setValue(attendanceStatus)
    }
}