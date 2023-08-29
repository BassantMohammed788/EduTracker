package com.example.edutracker.teacher.students.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StudentsViewModel(val repo:RepositoryInterface):ViewModel() {
    private var addStudentMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val addStudent: StateFlow<FirebaseState<Boolean>> = addStudentMutable

    private var getStudentMutable: MutableStateFlow<FirebaseState<List<Student>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getStudent: StateFlow<FirebaseState<List<Student>>> = getStudentMutable

    fun addStudent(student: Student, teacher_id: String, semester: String, grade_level: String, group_id: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.addStudent(student,teacher_id, semester,grade_level,group_id)
                ?.catch { e ->
                    Log.e("TAG", "addStudent: $e")
                    addStudentMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "addStudentSuccess: $data")
                    addStudentMutable.value = FirebaseState.Success(data)
                }
        }}

    fun getAllStudents(teacher_id: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllStudents(teacher_id)
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
