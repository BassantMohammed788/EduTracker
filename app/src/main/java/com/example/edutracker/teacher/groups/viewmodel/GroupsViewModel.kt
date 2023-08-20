package com.example.edutracker.teacher.groups.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GroupsViewModel(private val repo: RepositoryInterface): ViewModel() {
    private var addGroupMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val addGroup: StateFlow<FirebaseState<Boolean>> = addGroupMutable

    private var getGroupsMutable: MutableStateFlow<FirebaseState<List<Group>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getGroups: StateFlow<FirebaseState<List<Group>>> = getGroupsMutable
    fun addGroup(group: Group,teacher_id: String,semester:String){
        viewModelScope.launch(Dispatchers.IO){
            repo.addGroup(group,teacher_id,semester)
                ?.catch { e ->
                    Log.e("TAG", "addGroup: $e")
                    addGroupMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "addGroupSuccess: $data")
                    addGroupMutable.value = FirebaseState.Success(data)
                }
        }}
    fun getAllGroups(semester: String,teacher_id:String,grade_level: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllGroups(semester,teacher_id,grade_level)
                ?.catch { e ->
                    Log.e("TAG", "getAllGroups: $e")
                    getGroupsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllGroups: $data")
                    getGroupsMutable.value = FirebaseState.Success(data)
                }
        }}
    fun deleteGroup(semester: String,teacher_id:String,grade_level: String,group_id:String){
        viewModelScope.launch(Dispatchers.IO){
            repo.deleteGroup(semester,teacher_id,grade_level,group_id)
        }
    }
}