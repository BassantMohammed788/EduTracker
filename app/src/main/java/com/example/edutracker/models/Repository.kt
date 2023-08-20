package com.example.edutracker.models

import android.content.Context
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.network.RemoteInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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
}