package com.example.edutracker.models

import android.content.Context
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import kotlinx.coroutines.flow.Flow

interface RepositoryInterface {
    fun addAssistant(assistant: Assistant, teacher_id:String):Flow<Boolean>
    fun getAllAssistants(teacher_id:String): Flow<List<Assistant>>
    fun getAssistantByEmail(teacher_id: String, email: String): Flow<Assistant>
    fun deleteAssistantByEmail(teacher_id: String, email: String)
    fun updateAssistantData(teacher_id: String, email: String, updatedAssistant: Assistant):Flow<Assistant>
    fun addGroup(group: Group, teacher_id: String, semester:String):Flow<Boolean>
}