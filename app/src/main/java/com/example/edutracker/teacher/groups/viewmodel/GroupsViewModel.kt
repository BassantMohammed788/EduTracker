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
}