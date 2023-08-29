package com.example.edutracker.teacher.students.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.edutracker.models.RepositoryInterface


@Suppress("UNCHECKED_CAST")
class StudentsViewModelFactory (private val repo: RepositoryInterface) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(StudentsViewModel::class.java)){
            StudentsViewModel(repo) as T
        }else{
            throw IllegalArgumentException("ViewModel class not found")
        }
    }
}