package com.example.edutracker.teacher.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.dataclasses.Teacher
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
    val getGradeAndGroupCounts: StateFlow<FirebaseState<Pair<Int, Int>>> = getGradeAndGroupCountsMutable

    private var getTeacherMutable: MutableStateFlow<FirebaseState<Teacher>> =
        MutableStateFlow(FirebaseState.Loading)
    val getTeacher: StateFlow<FirebaseState<Teacher>> = getTeacherMutable

    private var getStudentMutable: MutableStateFlow<FirebaseState<List<Student>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getStudent: StateFlow<FirebaseState<List<Student>>> = getStudentMutable


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
    fun getTeacherById(teacherId: String){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTeacherById(teacherId)
                .catch { e ->
                    Log.e("TAG", "getTeacherByIdFailure: $e")
                    getTeacherMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getTeacherByIdSuccess: $data")
                    getTeacherMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun getAllStudents(teacher_id: String,semester: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllStudents(teacher_id,semester)
                ?.catch { e ->
                    Log.e("TAG", "getAllStudentsOfGroup: $e")
                    getStudentMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllStudentsOfGroupSuccess: $data")
                    getStudentMutable.value = FirebaseState.Success(data)
                }
        }}



}