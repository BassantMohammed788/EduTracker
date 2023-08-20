package com.example.edutracker.teacher.assistantdata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.edutracker.models.RepositoryInterface

class AssistantDataViewModelFactory (private val repo: RepositoryInterface) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AssistantDataViewModel::class.java)){
            AssistantDataViewModel(repo) as T
        }else{
            throw IllegalArgumentException("ViewModel class not found")
        }
    }
}