package com.example.edutracker.models

import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import com.example.edutracker.dataclasses.Student
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

    override fun getAssistantByEmail(teacher_id: String, email: String): Flow<Assistant> {
        return remoteSource.getAssistantByEmail(teacher_id,email)
    }

    override fun deleteAssistantByEmail(teacher_id: String, email: String) {
        remoteSource.deleteAssistantByEmail(teacher_id,email)
    }

    override fun updateAssistantData(teacher_id: String, email: String, updatedAssistant: Assistant): Flow<Assistant> {
        return remoteSource.updateAssistantData(teacher_id,email,updatedAssistant)
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

    override fun getAllStudents(teacher_id: String): Flow<List<Student>> {
        return remoteSource.getAllStudents(teacher_id)
    }
    override fun getAllStudentsOfGroup(teacher_id: String, group_id: String): Flow<List<Student>> {
        return remoteSource.getAllStudentsOfGroup(teacher_id, group_id)
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
}