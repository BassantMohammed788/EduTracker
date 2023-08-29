package com.example.edutracker.teacher.attendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.edutracker.models.RepositoryInterface

@Suppress("UNCHECKED_CAST")
class AttendanceViewModelFactory (private val repo: RepositoryInterface) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)){
            AttendanceViewModel(repo) as T
        }else{
            throw IllegalArgumentException("ViewModel class not found")
        }
    }
}