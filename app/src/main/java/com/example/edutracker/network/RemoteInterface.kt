package com.example.edutracker.network

import android.content.Context
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

}

