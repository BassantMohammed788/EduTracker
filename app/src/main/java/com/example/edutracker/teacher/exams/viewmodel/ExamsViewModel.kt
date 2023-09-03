package com.example.edutracker.teacher.exams.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.Exam
import com.example.edutracker.dataclasses.ExamDegree
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ExamsViewModel (private val repo: RepositoryInterface): ViewModel() {
    private var getExamsMutable: MutableStateFlow<FirebaseState<List<Exam>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getAllExams: StateFlow<FirebaseState<List<Exam>>> = getExamsMutable

    private var getExamDegreesMutable: MutableStateFlow<FirebaseState<List<Triple<String, String, ExamDegree>>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getExamDegrees: StateFlow<FirebaseState<List<Triple<String, String, ExamDegree>>>> = getExamDegreesMutable

    private var addExamDegreesMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val addExamDegrees: StateFlow<FirebaseState<Boolean>> = addExamDegreesMutable

    fun addExam(teacher_id: String, semester: String, grade_level: String, exam: Exam){
        viewModelScope.launch {
            repo.addExam(teacher_id, semester, grade_level, exam)
        }
    }
    fun getAllExams( teacher_id: String, semester: String,grade_level: String,group_id:String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllExams(teacher_id, semester, grade_level, group_id)
                .catch { e ->
                    Log.e("TAG", "getAllExams: $e")
                    getExamsMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getAllExams: $data")
                    getExamsMutable.value = FirebaseState.Success(data)
                }
        }
    }

    fun getStudentsExamDegreeForGroup(semester: String, gradeLevel: String, groupId: String, examId: String, studentList: List<Student>) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getStudentsExamDegreeForGroup(semester, gradeLevel, groupId, examId, studentList)
                .catch { e ->
                    Log.e("TAG", "getAllExams: $e")
                    getExamDegreesMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getAllExams: $data")
                    getExamDegreesMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun addExamDegree(semester: String, gradeLevel: String, examId: String, studentId: String, examDegree: ExamDegree){
        viewModelScope.launch(Dispatchers.IO) {
            repo.addExamDegree(semester, gradeLevel, examId, studentId, examDegree)
                .catch { e ->
                    Log.e("TAG", "getAllExams: $e")
                    addExamDegreesMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getAllExams: $data")
                    addExamDegreesMutable.value = FirebaseState.Success(data)
                }
        }
    }

}