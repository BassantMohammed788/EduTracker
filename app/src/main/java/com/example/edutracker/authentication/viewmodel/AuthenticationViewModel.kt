package com.example.edutracker.authentication.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.dataclasses.Teacher
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val repo: RepositoryInterface): ViewModel() {
    private var signInAssistantMutable: MutableStateFlow<FirebaseState<Pair<Any?, Boolean>>> =
        MutableStateFlow(FirebaseState.Loading)
    val signInAssistant: StateFlow<FirebaseState<Pair<Any?, Boolean>>>  = signInAssistantMutable

    private var signInStudentMutable: MutableStateFlow<FirebaseState<Pair<Any?, Boolean>>> =
        MutableStateFlow(FirebaseState.Loading)
    val signInStudent: StateFlow<FirebaseState<Pair<Any?, Boolean>>>  = signInStudentMutable


    private var getStudentMutable: MutableStateFlow<FirebaseState<Student?>> =
        MutableStateFlow(FirebaseState.Loading)
    val getStudent: StateFlow<FirebaseState<Student?>>  = getStudentMutable



    fun assistantSignIn(email: String, password: String, context: Context, callback: (Pair<Any?, Boolean>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.assistantSignIn(email, password,context,callback)

        }
    }

    fun studentSignIn(email: String, password: String, context: Context, callback: (Pair<Any?, Boolean>) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            repo.studentSignIn(email, password,context,callback)

        }
    }
    fun parentSignIn(email: String, password: String, context: Context, callback: (Pair<Any?, Boolean>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.parentSignIn(email, password,context,callback)
        }
    }

    fun getStudentById(studentId:String){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getStudentById(studentId)
                .catch { e ->
                    Log.e("TAG", "parentSignInFail: $e")
                    getStudentMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "parentSignInSuccess: $data")
                    getStudentMutable.value = FirebaseState.Success(data)
                }
        }
    }
}