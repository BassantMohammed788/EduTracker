package com.example.edutracker.teacher.lessons.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Lesson
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class LessonsViewModel(private val repo: RepositoryInterface): ViewModel() {
    private var addLessonMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val addLesson: StateFlow<FirebaseState<Boolean>> = addLessonMutable

    private var getAllLessonssMutable: MutableStateFlow<FirebaseState<List<Lesson>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getAllLessons: StateFlow<FirebaseState<List<Lesson>>> = getAllLessonssMutable

    private var getAllMonthsMutable: MutableStateFlow<FirebaseState<List<String>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getAllMonths: StateFlow<FirebaseState<List<String>>> = getAllMonthsMutable

    fun  addLesson( teacher_id: String, semester: String,grade_level: String,month:String,lesson: Lesson){
        viewModelScope.launch(Dispatchers.IO){
            repo.addLesson(teacher_id, semester, grade_level, month, lesson)
                .catch { e ->
                    Log.e("TAG", "addLesson: $e")
                    addLessonMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "addLessonSuccess: $data")
                    addLessonMutable.value = FirebaseState.Success(data)
                }
        }}

    fun getAllLessons( teacher_id: String, semester: String,grade_level: String,group_id:String,month:String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllLessons(teacher_id, semester, grade_level, group_id, month)
                ?.catch { e ->
                    Log.e("TAG", "getAllLessons: $e")
                    getAllLessonssMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllLessons: $data")
                    getAllLessonssMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun getAllMonths(semester: String, teacherId: String,grade_level: String,group_id: String)  {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllMonths(semester, teacherId, grade_level, group_id)
                ?.catch { e ->
                    Log.e("TAG", "getAllMonths: $e")
                    getAllMonthsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllMonths: $data")
                    getAllMonthsMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun deleteLesson(semester: String, teacher_id: String, gradeLevel: String, group_id: String,month: String,lesson_id:String){
        viewModelScope.launch(Dispatchers.IO){
            repo.deleteLesson(semester,teacher_id,gradeLevel, group_id, month, lesson_id)
        }
    }
}