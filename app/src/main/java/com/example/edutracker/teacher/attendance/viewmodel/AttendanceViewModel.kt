package com.example.edutracker.teacher.attendance.viewmodel

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

class AttendanceViewModel (val repo: RepositoryInterface): ViewModel() {
    private var getLessonAttendanceMutable: MutableStateFlow<FirebaseState<List<Triple<String, String, String>>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getLessonAttendance: StateFlow<FirebaseState<List<Triple<String, String, String>>>> = getLessonAttendanceMutable


    fun getLessonAttendance(semester: String, gradeLevel: String, groupId: String, lessonId: String, month:String,students:List<Student>){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getStudentAttendanceForLesson(semester, gradeLevel, groupId, lessonId, month,students)
                ?.catch { e ->
                    Log.e("TAG", "getLessonAttendance: $e")
                    getLessonAttendanceMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getLessonAttendanceSuccess: $data")
                    getLessonAttendanceMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun  addStudentAttendanceForLesson(semester: String, gradeLevel: String, groupId: String, lessonId: String, month: String, studentId: String, attendanceStatus: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addStudentAttendanceForLesson(semester, gradeLevel, groupId, lessonId, month, studentId, attendanceStatus)
        }

    }
}