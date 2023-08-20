package com.example.edutracker.teacher.assistantdata.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AssistantDataViewModel(private val repo: RepositoryInterface): ViewModel() {
    private var addAssistantsMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val addAssistant: StateFlow<FirebaseState<Boolean>> = addAssistantsMutable
    private var getAllAssistantsMutable: MutableStateFlow<FirebaseState<List<Assistant>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getAllAssistants: StateFlow<FirebaseState<List<Assistant>>> = getAllAssistantsMutable

    private var oneAssistantsMutable: MutableStateFlow<FirebaseState<Assistant>> =
        MutableStateFlow(FirebaseState.Loading)
    val oneAssistants: StateFlow<FirebaseState<Assistant>> = oneAssistantsMutable

    private var updateAssistantsMutable: MutableStateFlow<FirebaseState<Assistant>> =
        MutableStateFlow(FirebaseState.Loading)
    val updateAssistant: StateFlow<FirebaseState<Assistant>> = updateAssistantsMutable

    fun getAllAssistants(teacher_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllAssistants(teacher_id)
                ?.catch { e ->
                    Log.e("TAG", "getAllAssistantsFail: $e")
                    getAllAssistantsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllAssistantsSuccess: $data")
                    getAllAssistantsMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun getAssistantByEmail(teacher_id: String,email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAssistantByEmail(teacher_id,email)
                ?.catch { e ->
                    Log.e("TAG", "getAllAssistantsFail: $e")
                    oneAssistantsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllAssistantsSuccess: $data")
                    oneAssistantsMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun updateAssistant(teacher_id: String,email: String,assistant: Assistant) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateAssistantData(teacher_id,email,assistant)
                ?.catch { e ->
                    Log.e("TAG", "updateAssistant: $e")
                    updateAssistantsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllAssistantsSuccess: $data")
                    updateAssistantsMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun addAssistant(teacher_id: String,assistant: Assistant){
        viewModelScope.launch(Dispatchers.IO){
        repo.addAssistant(assistant,teacher_id)
            ?.catch { e ->
                Log.e("TAG", "updateAssistant: $e")
                addAssistantsMutable.value = FirebaseState.Failure(e)
            }
            ?.collect { data ->
                Log.i("TAG", "getAllAssistantsSuccess: $data")
                addAssistantsMutable.value = FirebaseState.Success(data)
            }
    }}
    fun deleteAssistant(teacher_id: String,email:String){
        viewModelScope.launch(Dispatchers.IO){
            repo.deleteAssistantByEmail(teacher_id,email)
        }
    }
}