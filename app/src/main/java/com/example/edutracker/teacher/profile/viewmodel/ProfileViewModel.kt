package com.example.edutracker.teacher.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class ProfileViewModel (val repo: RepositoryInterface): ViewModel() {
    private var getGradeAndGroupCountsMutable: MutableStateFlow<FirebaseState<Pair<Int, Int>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getGradeAndGroupCounts: StateFlow<FirebaseState<Pair<Int, Int>>> =
        getGradeAndGroupCountsMutable

    fun getGradesAndGroupsCounts(semester: String, teacherId: String){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getGradeAndGroupCounts(semester,teacherId)
                .catch { e ->
                    Log.e("TAG", "getGradeAndGroupCounts: $e")
                    getGradeAndGroupCountsMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getGradeAndGroupCountsSuccess: $data")
                    getGradeAndGroupCountsMutable.value = FirebaseState.Success(data)
                }
        }
    }

}