package com.example.edutracker.teacher.groups.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.edutracker.models.RepositoryInterface
class GroupsViewModelFactory (private val repo: RepositoryInterface) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(GroupsViewModel::class.java)){
            GroupsViewModel(repo) as T
        }else{
            throw IllegalArgumentException("ViewModel class not found")
        }
    }
}