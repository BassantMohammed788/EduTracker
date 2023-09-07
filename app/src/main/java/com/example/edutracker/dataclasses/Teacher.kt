package com.example.edutracker.dataclasses

import java.io.Serializable

data class Teacher(
    val id: String,
    val name: String,
    val phone: String,
    val password: String,
    val email: String,
    val subject: String
){
    constructor():this("","","","","","")
}

data class YearNode(
    val id: String
)
data class Parent(
    val parentEmail: String,
    val parentPassword: String,
    val parentPhone: String,
    val studentId: String,
    val teacherId: String
){
    constructor() : this("","", "", "","")
}

data class Student(
    val email: String,
    val name: String,
    val teacherId: String,
    val password: String,
    val phoneNumber: String,
    val activeGradeLevel: String,
    val activeGroupId: String,
    val activeSemester:String
) : Serializable {
    constructor() : this( "", "", "", "", "", "", "", "")
}
data class Assistant(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val teacherId: String
): Serializable{
    constructor() : this("", "","","","")
}

data class Group(
    val teacherId: String,
    val name: String,
    val id: String,
    val gradeLevel:String
){
    constructor() : this("", "","","")
}

data class Lesson(
    val id: String,
    val groupId: String,
    val teacherId: String,
    val time: String
){
    constructor() : this("", "","","")
}

data class StudentStatus(
    val studentId: String,
    val lessonId: String,
    val status: String
)
data class ExamDegree(
    val examDegree:String,
    val examName:String
){
    constructor(): this( "","")
}
data class Exam(
    val id: String,
    val groupId: String,
    val examName: String,

) : Serializable {
    constructor() : this( "","","")
}

data class AttendanceDetails(
   val attendanceStatus:String,
   val lessonId:String,
   val groupId:String,
   val month:String
){
    constructor() : this("", "","","")
}