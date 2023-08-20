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
    val teacherId: String,
    val id: String,
    val name: String,
    val password: String,
    val phoneNumber: String,
    val parentId: String,
    val parentPassword: String,
    val parentPhone: String,
    val grade: String,
    val groupId: String
)

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
    val date: String,
    val time: String
){
    constructor() : this("", "","","","")
}

data class StudentStatus(
    val studentId: String,
    val lessonId: String,
    val status: String
)