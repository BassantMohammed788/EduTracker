package com.example.edutracker.dataclasses

data class Teacher(
    val id: String,
    val name: String,
    val phone: String,
    val password: String,
    val email: String,
    val subject: String
)

data class YearNode(
    val id: String
)

data class Student(
    val email: String,
    val name: String,
    val teacherId: String,
    val password: String,
    val phoneNumber: String,
    val parentEmail: String,
    val parentPassword: String,
    val parentPhone: String,
    val activeGradeLevel: String,
    val activeGroupId: String
){
    constructor():this("","","","","","","","","","")
}

data class Assistant(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val teacherId: String
){
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
data class GroupStatus(
    val groupId: String,
    val status:String
){
    constructor(): this("","")
}