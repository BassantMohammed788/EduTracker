package com.example.edutracker.network

import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import com.example.edutracker.dataclasses.Student
import kotlinx.coroutines.flow.Flow

interface RemoteInterface {
    fun addAssistant(assistant: Assistant, teacher_id: String):Flow<Boolean>
    fun getAllAssistants(teacher_id: String): Flow<List<Assistant>>
    fun getAssistantByEmail(teacher_id: String, email: String): Flow<Assistant>
    fun deleteAssistantByEmail(teacher_id: String, email: String)
    fun updateAssistantData(teacher_id: String, email: String, updatedAssistant: Assistant): Flow<Assistant>
    fun addGroup(group: Group, teacher_id: String,semester:String):Flow<Boolean>
    fun getAllGroups(semester:String,teacher_id: String,gradeLevel:String): Flow<List<Group>>
    fun deleteGroup(semester:String,teacher_id: String,gradeLevel:String,group_id:String)
    fun addLesson( teacher_id: String, semester: String,grade_level: String,month:String,lesson: Lesson): Flow<Boolean>

    fun getAllGrades(semester: String, teacher_id: String): Flow<List<String>>
    fun getAllMonths(semester: String, teacherId: String,grade_level: String,group_id: String) : Flow<List<String>>
    fun getAllLessons( teacher_id: String, semester: String,grade_level: String,group_id:String,month:String): Flow<List<Lesson>>
    fun deleteLesson(semester: String, teacher_id: String, gradeLevel: String, group_id: String,month: String,lesson_id:String)
    fun addStudent(student: Student, teacher_id: String, semester: String, grade_level: String, group_id: String): Flow<Boolean>
    fun getAllStudents(teacher_id: String): Flow<List<Student>>
    fun getAllStudentsOfGroup(teacher_id: String, group_id: String): Flow<List<Student>>
    fun getStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentList: List<Student>): Flow<List<Triple<String, String, String>>>
    fun  addStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentId: String, attendanceStatus: String)
}

