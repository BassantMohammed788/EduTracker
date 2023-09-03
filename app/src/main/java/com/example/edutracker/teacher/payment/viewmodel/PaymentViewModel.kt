package com.example.edutracker.teacher.payment.viewmodel

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


class PaymentViewModel (val repo: RepositoryInterface): ViewModel() {
    private var getMonthPaymentMutable: MutableStateFlow<FirebaseState<List<Triple<String, String, String>>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getMonthPayment: StateFlow<FirebaseState<List<Triple<String, String, String>>>> = getMonthPaymentMutable


    fun getStudentsPaymentForMonth(semester: String, gradeLevel: String, month:String,students:List<Student>){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getStudentsPaymentForMonth(semester, gradeLevel, month,students)
                .catch { e ->
                    Log.e("TAG", "getStudentsPaymentForMonth: $e")
                    getMonthPaymentMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getStudentsPaymentForMonth: $data")
                    getMonthPaymentMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun  addStudentPaymentForMonth(semester: String, gradeLevel: String, month: String, studentId: String, attendanceStatus: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addStudentPaymentForMonth(semester, gradeLevel,month, studentId, attendanceStatus)
        }
    }
}