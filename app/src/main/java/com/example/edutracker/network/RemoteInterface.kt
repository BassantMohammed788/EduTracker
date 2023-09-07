package com.example.edutracker.network

import android.content.Context
import com.example.edutracker.dataclasses.*
import kotlinx.coroutines.flow.Flow

interface RemoteInterface {
    fun addAssistant(assistant: Assistant, teacher_id: String, callback: (Boolean) -> Unit)
    fun getAllAssistants(teacher_id: String): Flow<List<Assistant>>
    fun deleteAssistantByEmail(teacher_id: String, email: String)
    fun updateAssistantData(updatedAssistant: Assistant):Flow<Boolean>
    fun addGroup(group: Group, teacher_id: String,semester:String):Flow<Boolean>
    fun getAllGroups(semester:String,teacher_id: String,gradeLevel:String): Flow<List<Group>>
    fun deleteGroup(semester:String,teacher_id: String,gradeLevel:String,group_id:String)
    fun addLesson( teacher_id: String, semester: String,grade_level: String,month:String,lesson: Lesson): Flow<Boolean>
    fun getAllGrades(semester: String, teacherId: String): Flow<List<String>>
    fun getAllMonths(semester: String, teacherId: String,grade_level: String,group_id: String) : Flow<List<String>>
    fun getAllLessons( teacher_id: String, semester: String,grade_level: String,group_id:String,month:String): Flow<List<Lesson>>
    fun deleteLesson(semester: String, teacher_id: String, gradeLevel: String, group_id: String,month: String,lesson_id:String)
    fun addStudent(student: Student, teacher_id: String, semester: String, grade_level: String, group_id: String,callback: (Boolean) -> Unit)
    fun getAllStudents(teacher_id: String,semester: String): Flow<List<Student>>
    fun getAllStudentsOfGroup(teacher_id: String, group_id: String,semester: String): Flow<List<Student>>
    fun getStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentList: List<Student>): Flow<List<Triple<String, String, String>>>
    fun  addStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentId: String, attendanceStatus: String)
    fun getStudentsPaymentForMonth(semester: String, gradeLevel: String,  month: String, studentList: List<Student>): Flow<List<Triple<String, String, String>>>
    fun addStudentPaymentForMonth(semester: String, gradeLevel: String,  month: String, studentId: String, paymentStatus: String)
    fun getStudentPaymentStatusForAllMonths(studentId: String, semester: String): Flow<List<Pair<String, String>>>
    fun deleteStudent( email: String)
    fun updateStudentData(updatedStudent: Student)
    fun moveStudentToNewGroup(updatedStudent: Student):Flow<Boolean>
    fun getStudentAttendanceDetails(studentId: String, semester: String, gradeLevel: String): Flow<List<AttendanceDetails>>
    fun getLessonTimeAndAttendanceStatus(teacherId: String, semester: String, gradeLevel: String, attendanceDetailsList: List<AttendanceDetails>): Flow<List<Pair<String, String>>>

    fun addExam(teacher_id: String, semester: String, grade_level: String, exam: Exam):Flow<Boolean>
    fun getAllExams(teacher_id: String, semester: String, grade_level: String, group_id: String): Flow<List<Exam>>
    fun addExamDegree(semester: String, gradeLevel: String, examId: String, studentId: String, examDegree: ExamDegree): Flow<Boolean>
    fun getStudentsExamDegreeForGroup(semester: String, gradeLevel: String, groupId: String, examId: String, studentList: List<Student>): Flow<List<Triple<String, String, ExamDegree>>>
    fun getStudentExamsDegrees(studentId: String, semester: String): Flow<List<ExamDegree>>

    fun addParent(parent: Parent, teacherId: String, studentId: String, callback: (Boolean) -> Unit)
    fun getParentByEmail(studentId: String): Flow<Parent?>
    fun deleteParent(studentId: String, callback: (Boolean) -> Unit)
    fun updateParent(updatedParent: Parent) :Flow<Boolean>
    fun getAllExistingStudents(teacher_id: String,semester: String): Flow<List<Student>>
    fun addStudentToNewSemester(updatedStudent: Student) :Flow<Boolean>
    fun getGradeAndGroupCounts(semester: String, teacherId: String): Flow<Pair<Int, Int>>
    fun getTeacherById(teacherId: String): Flow<Teacher>
    fun isParentExist(email: String): Flow<Boolean>
    fun assistantSignIn(email: String, password: String, context: Context, callback: (Pair<Any?, Boolean>) -> Unit)
    fun studentSignIn(email: String, password: String, context: Context, callback: (Pair<Any?, Boolean>) -> Unit)
    fun parentSignIn(email: String, password: String, context: Context, callback: (Pair<Any?, Boolean>) -> Unit)
    fun getStudentById(studentId: String): Flow<Student?>
}

