package com.example.edutracker.network
import com.example.edutracker.dataclasses.*
import com.example.edutracker.utilities.Constants
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
            val assistantRef = databaseReference.child(Constants.ASSISTANT_NODE).child(assistant.email)

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
        val assistantsRef = databaseReference.child(Constants.ASSISTANT_NODE)
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

    override fun updateAssistantData(updatedAssistant: Assistant):Flow<Boolean> =callbackFlow {
        val assistantRef = databaseReference.child(Constants.ASSISTANT_NODE).child(updatedAssistant.email)
        val updatedData = HashMap<String, Any>()
        updatedData["name"] =updatedAssistant.name
        updatedData["phone"] = updatedAssistant.phone
        updatedData["password"] = updatedAssistant.password
        assistantRef.updateChildren(updatedData)
            .addOnSuccessListener {
                // Addition successful
                trySend(true)
                close()
            }
            .addOnFailureListener { exception ->
                // Addition failed
                trySend(false)
                close(exception)
            }

        awaitClose()
    }
    override fun deleteAssistantByEmail(teacher_id: String, email: String) {
        val assistantRef = databaseReference.child(Constants.ASSISTANT_NODE).child(email)
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

    override fun deleteLesson(semester: String, teacher_id: String, gradeLevel: String, group_id: String, month: String, lesson_id: String) {
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
                           /* val dataRef = studentRef.child(Constants.SEMESTER_NODE).child(semester)
                                .child(Constants.GRADE_NODE).child(grade_level)
                                .child(Constants.GROUP_NODE).child(group_id).child("SStatus")
                            dataRef.setValue(GroupStatus("active"))*/
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

    override fun getAllStudents(teacher_id: String,semester: String): Flow<List<Student>> = callbackFlow {
        val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

        val studentsList = mutableListOf<Student>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                studentsList.clear()
                for (studentSnapshot in dataSnapshot.children) {
                    val student = studentSnapshot.getValue(Student::class.java)
                    student?.let {
                        if (it.teacherId == teacher_id&&it.activeSemester==semester) {
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

    override fun getAllStudentsOfGroup(teacher_id: String, group_id: String,semester:String): Flow<List<Student>> =
        callbackFlow {
            val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

            val studentsList = mutableListOf<Student>()

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    studentsList.clear()
                    for (studentSnapshot in dataSnapshot.children) {
                        val student = studentSnapshot.getValue(Student::class.java)
                        student?.let {
                            if (it.teacherId == teacher_id && it.activeGroupId == group_id&&it.activeSemester==semester) {
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

    override fun getStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentList: List<Student>): Flow<List<Triple<String, String, String>>> = callbackFlow {
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
                        .child(Constants.ATTENDANCE_STATUS_NODE)
                        .getValue(String::class.java) ?: "null"
                    val studentName = student.name
                    studentAttendanceList.add(Triple(studentId, studentName, attendanceStatus))

                }
                trySend(studentAttendanceList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentsRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }

    override fun addStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentId: String, attendanceStatus: String) {
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
            .child(Constants.ATTENDANCE_STATUS_NODE)

        studentRef.setValue(attendanceStatus)
    }

    override fun getStudentsPaymentForMonth(
        semester: String,
        gradeLevel: String,
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
                    val paymentSnapshot = studentSnapshot
                        .child(Constants.SEMESTER_NODE)
                        .child(semester)
                        .child(Constants.PAYMENT_STATUS_NODE)
                        .child(month)
                    val paymentStatus = paymentSnapshot
                        .getValue(String::class.java) ?: "null"
                    val studentName = student.name

                    studentAttendanceList.add(Triple(studentId, studentName, paymentStatus))

                }
                trySend(studentAttendanceList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentsRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }

    override fun addStudentPaymentForMonth(semester: String, gradeLevel: String, month: String, studentId: String, paymentStatus: String) {
        val studentRef = databaseReference
            .child(Constants.STUDENT_NODE)
            .child(studentId)
            .child(Constants.SEMESTER_NODE)
            .child(semester)
            .child(Constants.PAYMENT_STATUS_NODE)
            .child(month)
        studentRef.setValue(paymentStatus)
    }

    override fun getStudentPaymentStatusForAllMonths(studentId: String, semester: String): Flow<List<Pair<String, String>>> = callbackFlow {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(studentId)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val paymentStatusSnapshot = dataSnapshot
                    .child(Constants.SEMESTER_NODE)
                    .child(semester)
                    .child(Constants.PAYMENT_STATUS_NODE)

                val paymentStatusList = mutableListOf<Pair<String, String>>()
                for (monthSnapshot in paymentStatusSnapshot.children) {
                    val month = monthSnapshot.key ?: "null"
                    val paymentStatus = monthSnapshot.getValue(String::class.java) ?: "null"
                    paymentStatusList.add(Pair(month, paymentStatus))
                }

                trySend(paymentStatusList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentRef.removeEventListener(valueEventListener) }
    }
    override fun deleteStudent(email: String) {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(email)
        studentRef.removeValue()
    }

    override fun updateStudentData(updatedStudent: Student) {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(updatedStudent.email)
        studentRef.setValue(updatedStudent)
    }
    override fun moveStudentToNewGroup(updatedStudent: Student, semester: String, oldGroupId: String) {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(updatedStudent.email)
        val updatedData = HashMap<String, Any>()
        updatedData["name"] = updatedStudent.name
        updatedData["email"] = updatedStudent.email
        updatedData["activeGroupId"] = updatedStudent.activeGroupId
        updatedData["activeGradeLevel"] = updatedStudent.activeGradeLevel
      updatedData["phoneNumber"] = updatedStudent.phoneNumber
        updatedData["password"] = updatedStudent.password

        studentRef.updateChildren(updatedData)
    }

    override fun getStudentAttendanceDetails(
        studentId: String,
        semester: String,
        gradeLevel: String
    ): Flow<List<AttendanceDetails>> = callbackFlow {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(studentId)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val attendanceDetailsList = mutableListOf<AttendanceDetails>()

                val semesterSnapshot = dataSnapshot.child(Constants.SEMESTER_NODE).child(semester)
                val gradeSnapshot = semesterSnapshot.child(Constants.GRADE_NODE).child(gradeLevel)

                for (groupSnapshot in gradeSnapshot.child(Constants.GROUP_NODE).children) {
                    val groupId = groupSnapshot.key ?: continue // Skip if groupId is null

                    for (monthSnapshot in groupSnapshot.children) {
                        val month = monthSnapshot.key ?: continue // Skip if month is null

                        for (lessonSnapshot in monthSnapshot.children) {
                            val lessonId = lessonSnapshot.key ?: continue // Skip if lessonId is null

                            val attendanceStatus = lessonSnapshot.child(Constants.ATTENDANCE_STATUS_NODE)
                                .getValue(String::class.java) ?: "null"

                            val attendanceDetails = AttendanceDetails(
                                attendanceStatus,
                                lessonId,
                                groupId,
                                month
                            )
                            attendanceDetailsList.add(attendanceDetails)
                        }
                    }
                }

                trySend(attendanceDetailsList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentRef.removeEventListener(valueEventListener) }
    }
    override fun getLessonTimeAndAttendanceStatus(
        teacherId: String,
        semester: String,
        gradeLevel: String,
        attendanceDetailsList: List<AttendanceDetails>
    ): Flow<List<Pair<String, String>>> = callbackFlow {
        val teacherRef = databaseReference.child(Constants.TEACHER_NODE).child(teacherId)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lessonTimeAndAttendanceList = mutableListOf<Pair<String, String>>()

                for (attendanceDetails in attendanceDetailsList) {
                    val groupId = attendanceDetails.groupId
                    val month = attendanceDetails.month
                    val lessonId = attendanceDetails.lessonId

                    val lessonSnapshot = dataSnapshot.child(semester)
                        .child(Constants.GRADE_NODE)
                        .child(gradeLevel)
                        .child(groupId)
                        .child(month)
                        .child(Constants.LESSON_NODE)
                        .child(lessonId)

                    val lessonTime = lessonSnapshot.child("time").getValue(String::class.java) ?: "null"
                    val attendanceStatus = attendanceDetails.attendanceStatus

                    val pair = Pair(lessonTime, attendanceStatus)
                    lessonTimeAndAttendanceList.add(pair)
                }

                trySend(lessonTimeAndAttendanceList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        teacherRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { teacherRef.removeEventListener(valueEventListener) }
    }

    override fun addExam(teacher_id: String, semester: String, grade_level: String, exam: Exam)
    {
        val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(exam.groupId)
            .child(Constants.EXAM_NODE).child(exam.id)
        lessonRef.setValue(exam)
    }
    override fun getAllExams(teacher_id: String, semester: String, grade_level: String, group_id: String): Flow<List<Exam>> = callbackFlow {
        val lessonRef = databaseReference.child(Constants.TEACHER_NODE).child(teacher_id)
            .child(semester).child(Constants.GRADE_NODE).child(grade_level).child(group_id)
            .child(Constants.EXAM_NODE)

        val examList = mutableListOf<Exam>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                examList.clear()
                for (assistantSnapshot in dataSnapshot.children) {
                    val assistant = assistantSnapshot.getValue(Exam::class.java)
                    assistant?.let { examList.add(it) }
                }
                trySend(examList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        lessonRef.addValueEventListener(valueEventListener)

         awaitClose { lessonRef.removeEventListener(valueEventListener) }
    }
    override fun addExamDegree(semester: String, gradeLevel: String, examId: String, studentId: String, examDegree: ExamDegree): Flow<Boolean> = callbackFlow {
        val studentRef = databaseReference
            .child(Constants.STUDENT_NODE)
            .child(studentId)
            .child(Constants.SEMESTER_NODE)
            .child(semester)
            .child(Constants.EXAM_NODE)
            .child(examId)

        studentRef.setValue(examDegree)
            .addOnSuccessListener {
                // Addition successful
                trySend(true)
                close()
            }
            .addOnFailureListener { exception ->
                // Addition failed
                trySend(false)
                close(exception)
            }

        awaitClose()
    }
    override fun getStudentsExamDegreeForGroup(
        semester: String,
        gradeLevel: String,
        groupId: String,
        examId: String,
        studentList: List<Student>
    ): Flow<List<Triple<String, String, ExamDegree>>> = callbackFlow {
        val examDegreeList = mutableListOf<Triple<String, String, ExamDegree>>()

        val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                examDegreeList.clear()
                for (student in studentList) {
                    val studentId = student.email
                    val studentSnapshot = dataSnapshot.child(studentId)
                    val examDegree = studentSnapshot
                        .child(Constants.SEMESTER_NODE)
                        .child(semester)
                        .child(Constants.EXAM_NODE)
                        .child(examId)
                        .getValue(ExamDegree::class.java) ?: ExamDegree("null","null")
                    val studentName = student.name
                    examDegree.let {
                        examDegreeList.add(Triple(studentId, studentName, it))
                    }
                }
                trySend(examDegreeList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentsRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }

    override fun getStudentExamsDegrees(studentId: String, semester: String): Flow<List<ExamDegree>> = callbackFlow {
        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(studentId)
            .child(Constants.SEMESTER_NODE)
            .child(semester)
            .child(Constants.EXAM_NODE)

        val examsList = mutableListOf<ExamDegree>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                examsList.clear()
                for (studentSnapshot in dataSnapshot.children) {
                    val examDegree = studentSnapshot.getValue(ExamDegree::class.java)
                    examDegree?.let {
                        examsList.add(it)
                    }
                }
                trySend(examsList.toList()).isSuccess // Emit the updated list
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { studentRef.removeEventListener(valueEventListener) }
    }

    override fun addParent(parent: Parent, teacherId: String, studentId: String): Flow<Boolean> = callbackFlow {
        val parentRef = databaseReference.child(Constants.PARENT_NODE).child(parent.parentEmail)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val parentExists = dataSnapshot.exists()
                if (parentExists) {
                    trySend(false)
                } else {
                    parentRef.setValue(parent)
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
        parentRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { parentRef.removeEventListener(valueEventListener) }
    }
    override fun getParentByEmail(studentId: String): Flow<Parent?> = callbackFlow {
        val parentRef = databaseReference.child(Constants.PARENT_NODE)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var parent: Parent? = null
                for (parentSnapshot in dataSnapshot.children) {
                    val parentObj = parentSnapshot.getValue(Parent::class.java)
                    if (parentObj?.studentId == studentId) {
                        parent = parentObj
                        break
                    }
                }
                trySend(parent).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        parentRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { parentRef.removeEventListener(valueEventListener) }
    }

    override fun updateParent(updatedParent: Parent) :Flow<Boolean> =callbackFlow {
        val parentRef = databaseReference.child(Constants.PARENT_NODE).child(updatedParent.parentEmail)
        val updatedData = HashMap<String, Any>()
        updatedData["parentPassword"] =updatedParent.parentPassword
        updatedData["parentPhone"] = updatedParent.parentPhone

        parentRef.updateChildren(updatedData)
            .addOnSuccessListener {
            // Addition successful
            trySend(true)
            close()
        }
            .addOnFailureListener { exception ->
                // Addition failed
                trySend(false)
                close(exception)
            }

        awaitClose()
    }
    override fun getAllExistingStudents(teacher_id: String,semester: String): Flow<List<Student>> = callbackFlow {
        val studentsRef = databaseReference.child(Constants.STUDENT_NODE)

        val studentsList = mutableListOf<Student>()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                studentsList.clear()
                for (studentSnapshot in dataSnapshot.children) {
                    val student = studentSnapshot.getValue(Student::class.java)
                    student?.let {
                        if (it.teacherId == teacher_id&&it.activeSemester!=semester) {
                            studentsList.add(it)
                        }
                    }
                }
                trySend(studentsList.toList()).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        studentsRef.addValueEventListener(valueEventListener)

        awaitClose { studentsRef.removeEventListener(valueEventListener) }
    }

    override fun addStudentToNewSemester(updatedStudent: Student) :Flow<Boolean> =callbackFlow {

        val studentRef = databaseReference.child(Constants.STUDENT_NODE).child(updatedStudent.email)
        val updatedData = HashMap<String, Any>()
        updatedData["activeGroupId"] = updatedStudent.activeGroupId
        updatedData["activeGradeLevel"] = updatedStudent.activeGradeLevel
        updatedData["activeSemester"] = updatedStudent.activeSemester
        studentRef.updateChildren(updatedData)
            .addOnSuccessListener {
                // Addition successful
                trySend(true)
                close()
            }
            .addOnFailureListener { exception ->
                // Addition failed
                trySend(false)
                close(exception)
            }

        awaitClose()
    }
    override fun getGradeAndGroupCounts(
        semester: String,
        teacherId: String
    ): Flow<Pair<Int, Int>> = callbackFlow {
        val groupsRef = databaseReference.child(Constants.TEACHER_NODE).child(teacherId)
            .child(semester).child(Constants.GRADE_NODE)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val gradeCount = dataSnapshot.childrenCount.toInt()
                var groupCount = 0

                for (gradeSnapshot in dataSnapshot.children) {
                    groupCount += gradeSnapshot.childrenCount.toInt()
                }

                trySend(Pair(gradeCount, groupCount)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        groupsRef.addListenerForSingleValueEvent(valueEventListener)

        awaitClose { groupsRef.removeEventListener(valueEventListener) }
    }

  }