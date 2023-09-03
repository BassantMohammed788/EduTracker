package com.example.edutracker.models

import com.example.edutracker.dataclasses.*
import com.example.edutracker.network.RemoteInterface
import kotlinx.coroutines.flow.Flow

class Repository private constructor( var remoteSource: RemoteInterface):RepositoryInterface  {
    companion object{
        private var instance : Repository? = null
        fun getInstance(remoteSource: RemoteInterface):Repository{
            return instance?: synchronized(this){
                val temp = Repository(remoteSource)
                instance = temp
                temp
            }
        }
    }
    override fun addAssistant(assistant: Assistant, teacher_id: String):Flow<Boolean> {
        return remoteSource.addAssistant(assistant,teacher_id)
    }

    override fun getAllAssistants(teacher_id: String): Flow<List<Assistant>> {
        return remoteSource.getAllAssistants(teacher_id)
    }

    override fun deleteAssistantByEmail(teacher_id: String, email: String) {
        remoteSource.deleteAssistantByEmail(teacher_id,email)
    }

    override fun updateAssistantData(updatedAssistant: Assistant): Flow<Boolean> {
        return remoteSource.updateAssistantData(updatedAssistant)
    }

    override fun addGroup(group: Group, teacher_id: String, semester: String): Flow<Boolean> {
        return remoteSource.addGroup(group,teacher_id, semester)
    }

    override fun getAllGroups(semester: String, teacher_id: String, gradeLevel: String): Flow<List<Group>> {
        return remoteSource.getAllGroups(semester, teacher_id, gradeLevel)
    }

    override fun deleteGroup(
        semester: String,
        teacher_id: String,
        gradeLevel: String,
        group_id: String
    ) {
        remoteSource.deleteGroup(semester, teacher_id, gradeLevel, group_id)
    }

    override fun getAllGrades(semester: String, teacher_id: String): Flow<List<String>> {
        return remoteSource.getAllGrades(semester, teacher_id)
    }

    override fun addLesson(
        teacher_id: String,
        semester: String,
        grade_level: String,
        month: String,
        lesson: Lesson
    ): Flow<Boolean> {
        return remoteSource.addLesson(teacher_id, semester, grade_level, month, lesson)
    }

    override fun getAllMonths(
        semester: String,
        teacherId: String,
        grade_level: String,
        group_id: String
    ): Flow<List<String>> {
        return remoteSource.getAllMonths(semester, teacherId, grade_level, group_id)
    }
    override fun getAllLessons(teacher_id: String, semester: String, grade_level: String, group_id: String, month: String): Flow<List<Lesson>>
    {
        return remoteSource.getAllLessons(teacher_id, semester, grade_level, group_id, month)
    }

    override fun deleteLesson(semester: String, teacher_id: String, gradeLevel: String, group_id: String, month: String, lesson_id: String) {
        remoteSource.deleteLesson(semester, teacher_id, gradeLevel, group_id, month, lesson_id)
    }

    override fun addStudent(
        student: Student,
        teacher_id: String,
        semester: String,
        grade_level: String,
        group_id: String
    ): Flow<Boolean> {
        return remoteSource.addStudent(student, teacher_id, semester, grade_level, group_id)
    }

    override fun getAllStudents(teacher_id: String,semester: String): Flow<List<Student>> {
        return remoteSource.getAllStudents(teacher_id,semester)
    }
    override fun getAllStudentsOfGroup(teacher_id: String, group_id: String,semester: String): Flow<List<Student>> {
        return remoteSource.getAllStudentsOfGroup(teacher_id, group_id,semester)
    }

    override fun getStudentAttendanceForLesson(
        semester: String,
        gradeLevel: String,
        groupId: String,
        lessonId: String,
        month: String,
        studentList: List<Student>
    ): Flow<List<Triple<String, String, String>>> {
        return remoteSource.getStudentAttendanceForLesson(semester, gradeLevel, groupId, lessonId, month,studentList)

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
        remoteSource.addStudentAttendanceForLesson(semester, gradeLevel, groupId, lessonId, month, studentId, attendanceStatus)
    }

    override fun getStudentsPaymentForMonth(
        semester: String,
        gradeLevel: String,
        month: String,
        studentList: List<Student>
    ): Flow<List<Triple<String, String, String>>> {
        return remoteSource.getStudentsPaymentForMonth(semester, gradeLevel, month, studentList)
    }

    override fun addStudentPaymentForMonth(
        semester: String,
        gradeLevel: String,
        month: String,
        studentId: String,
        paymentStatus: String
    ) {
        remoteSource.addStudentPaymentForMonth(semester, gradeLevel, month, studentId, paymentStatus)
    }

    override fun deleteStudent(email: String) {
        remoteSource.deleteStudent( email)
    }

    override fun updateStudentData( updatedStudent: Student) {
        remoteSource.updateStudentData( updatedStudent)
    }

    override fun moveStudentToNewGroup(
        updatedStudent: Student,
        semester: String,
        oldGroupId: String
    ) {
        remoteSource.moveStudentToNewGroup(updatedStudent, semester, oldGroupId)
    }

    override fun getStudentPaymentStatusForAllMonths(
        studentId: String,
        semester: String
    ): Flow<List<Pair<String, String>>> {
        return remoteSource.getStudentPaymentStatusForAllMonths(studentId, semester)
    }

    override fun getStudentAttendanceDetails(
        studentId: String,
        semester: String,
        gradeLevel: String
    ): Flow<List<AttendanceDetails>> {
        return remoteSource.getStudentAttendanceDetails(studentId, semester, gradeLevel)
    }

    override fun getLessonTimeAndAttendanceStatus(
        teacherId: String,
        semester: String,
        gradeLevel: String,
        attendanceDetailsList: List<AttendanceDetails>
    ): Flow<List<Pair<String, String>>> {
        return remoteSource.getLessonTimeAndAttendanceStatus(teacherId, semester, gradeLevel, attendanceDetailsList)
    }

    override fun addExam(teacher_id: String, semester: String, grade_level: String, exam: Exam) {
        remoteSource.addExam(teacher_id, semester, grade_level, exam)
    }

    override fun getAllExams(teacher_id: String, semester: String, grade_level: String, group_id: String): Flow<List<Exam>> {
        return remoteSource.getAllExams(teacher_id, semester, grade_level, group_id)
    }

    override fun addExamDegree(
        semester: String,
        gradeLevel: String,
        examId: String,
        studentId: String,
        examDegree: ExamDegree
    ): Flow<Boolean> {
        return remoteSource.addExamDegree(semester, gradeLevel, examId, studentId, examDegree)
    }

    override fun getStudentsExamDegreeForGroup(
        semester: String,
        gradeLevel: String,
        groupId: String,
        examId: String,
        studentList: List<Student>
    ): Flow<List<Triple<String, String, ExamDegree>>> {
        return remoteSource.getStudentsExamDegreeForGroup(semester, gradeLevel, groupId, examId, studentList)
    }

    override fun getStudentExamsDegrees(
        studentId: String,
        semester: String
    ): Flow<List<ExamDegree>> {
        return remoteSource.getStudentExamsDegrees(studentId, semester)
    }

    override fun addParent(parent: Parent, teacherId: String, studentId: String): Flow<Boolean> {
        return remoteSource.addParent(parent,teacherId, studentId)
    }

    override fun getParentByEmail(studentId: String): Flow<Parent?> {
        return remoteSource.getParentByEmail(studentId)
    }

    override fun updateParent(updatedParent: Parent): Flow<Boolean> {
        return remoteSource.updateParent(updatedParent)
    }

    override fun getAllExistingStudents(teacher_id: String, semester: String): Flow<List<Student>> {
        return remoteSource.getAllExistingStudents(teacher_id, semester)
    }

    override fun addStudentToNewSemester(updatedStudent: Student): Flow<Boolean> {
        return remoteSource.addStudentToNewSemester(updatedStudent)
    }

    override fun getGradeAndGroupCounts(semester: String, teacherId: String): Flow<Pair<Int, Int>> {
        return remoteSource.getGradeAndGroupCounts(semester, teacherId)
    }
}