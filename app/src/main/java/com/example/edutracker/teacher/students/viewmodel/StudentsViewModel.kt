package com.example.edutracker.teacher.students.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutracker.dataclasses.AttendanceDetails
import com.example.edutracker.dataclasses.ExamDegree
import com.example.edutracker.dataclasses.Parent
import com.example.edutracker.dataclasses.Student
import com.example.edutracker.models.RepositoryInterface
import com.example.edutracker.network.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StudentsViewModel(val repo:RepositoryInterface):ViewModel() {
    private var addStudentToNewSemesterMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val addStudentToNewSemester: StateFlow<FirebaseState<Boolean>> = addStudentToNewSemesterMutable

    private var getParentMutable: MutableStateFlow<FirebaseState<Parent?>> =
        MutableStateFlow(FirebaseState.Loading)
    val getParent: StateFlow<FirebaseState<Parent?>> = getParentMutable

    private var updateStudentMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val updateStudent: StateFlow<FirebaseState<Boolean>> = updateStudentMutable

    private var updateParentMutable: MutableStateFlow<FirebaseState<Boolean>> =
        MutableStateFlow(FirebaseState.Loading)
    val updateParent: StateFlow<FirebaseState<Boolean>> = updateParentMutable

    private var getStudentMutable: MutableStateFlow<FirebaseState<List<Student>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getStudent: StateFlow<FirebaseState<List<Student>>> = getStudentMutable

    private var getExistingStudentsMutable: MutableStateFlow<FirebaseState<List<Student>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getExistingStudents: StateFlow<FirebaseState<List<Student>>> = getExistingStudentsMutable

    private var getGroupStudentMutable: MutableStateFlow<FirebaseState<List<Student>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getGroupStudent: StateFlow<FirebaseState<List<Student>>> = getGroupStudentMutable

    private var getStudentPaymentStatusMutable: MutableStateFlow<FirebaseState<List<Pair<String, String>>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getStudentPaymentStatus: StateFlow<FirebaseState<List<Pair<String, String>>>> = getStudentPaymentStatusMutable

    private var getStudentAttendanceDetailsMutable: MutableStateFlow<FirebaseState<List<AttendanceDetails>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getStudentAttendanceDetails: StateFlow<FirebaseState<List<AttendanceDetails>>> = getStudentAttendanceDetailsMutable

    private var getLessonTimeAndAttendanceStatusMutable: MutableStateFlow<FirebaseState<List<Pair<String, String>>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getLessonTimeAndAttendanceStatus: StateFlow<FirebaseState<List<Pair<String, String>>>> = getLessonTimeAndAttendanceStatusMutable

    private var getExamDegreesMutable: MutableStateFlow<FirebaseState<List<ExamDegree>>> =
        MutableStateFlow(FirebaseState.Loading)
    val getExamDegrees: StateFlow<FirebaseState<List<ExamDegree>>> = getExamDegreesMutable



    fun addStudent(student: Student, teacher_id: String, semester: String, grade_level: String, group_id: String,callback: (Boolean) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            repo.addStudent(student,teacher_id, semester,grade_level,group_id,callback)
        }}

    fun getAllStudents(teacher_id: String,semester: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllStudents(teacher_id,semester)
                .catch { e ->
                    Log.e("TAG", "getAllStudentsOfGroup: $e")
                    getStudentMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getAllStudentsOfGroupSuccess: $data")
                    getStudentMutable.value = FirebaseState.Success(data)
                }
        }}
    fun getGroupStudents(teacher_id: String,group_id: String,semester: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllStudentsOfGroup(teacher_id,group_id,semester)
                .catch { e ->
                    Log.e("TAG", "getGroupStudents: $e")
                    getGroupStudentMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getGroupStudentsSuccess: $data")
                    getGroupStudentMutable.value = FirebaseState.Success(data)
                }
        }}

    fun deleteStudent(email:String){
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteStudent(email)
        }
    }

    fun  updateStudent(updatedStudent: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.moveStudentToNewGroup(updatedStudent)
                .catch { e ->
                    Log.e("TAG", "updateStudentFailure: $e")
                    updateStudentMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "updateStudentSuccess: $data")
                    updateStudentMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun getStudentPaymentStatusForAllMonths(studentId: String,semester: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getStudentPaymentStatusForAllMonths(studentId, semester)
                .catch { e ->
                    Log.e("TAG", "getGroupStudents: $e")
                    getStudentPaymentStatusMutable.value = FirebaseState.Failure(e)
                }
              .collect { data ->
                    Log.i("TAG", "getGroupStudentsSuccess: $data")
                    getStudentPaymentStatusMutable.value = FirebaseState.Success(data)
                }
        }}
    fun  addStudentPaymentForMonth(semester: String, gradeLevel: String, month: String, studentId: String, attendanceStatus: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addStudentPaymentForMonth(semester, gradeLevel,month, studentId, attendanceStatus)
        }
    }
    fun getStudentAttendanceDetails(studentId: String, semester: String, gradeLevel: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getStudentAttendanceDetails(studentId, semester,gradeLevel)
                ?.catch { e ->
                    Log.e("TAG", "getStudentAttendanceDetails: $e")
                    getStudentAttendanceDetailsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getStudentAttendanceDetailsSuccess: $data")
                    getStudentAttendanceDetailsMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun getLessonTimeAndAttendanceStatus(teacherId: String, semester: String, gradeLevel: String, attendanceDetailsList: List<AttendanceDetails>){
        viewModelScope.launch(Dispatchers.IO){
            repo.getLessonTimeAndAttendanceStatus(teacherId, semester, gradeLevel, attendanceDetailsList)
                .catch { e ->
                    Log.e("TAG", "getLessonTimeAndAttendanceStatus: $e")
                    getLessonTimeAndAttendanceStatusMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getLessonTimeAndAttendanceStatusSuccess: $data")
                    getLessonTimeAndAttendanceStatusMutable.value = FirebaseState.Success(data)
                }
        }
    }fun getStudentExamsDegrees(studentId: String, semester: String){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getStudentExamsDegrees(studentId, semester)
                .catch { e ->
                    Log.e("TAG", "getStudentExamsDegrees: $e")
                    getExamDegreesMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "getStudentExamsDegrees: $data")
                    getExamDegreesMutable.value = FirebaseState.Success(data)
                }
        }
    }

    fun addParent(parent: Parent, teacherId: String, studentId: String, callback: (Boolean) -> Unit) {
            repo.addParent(parent, teacherId, studentId,callback)
        }
    fun getParentByEmail(studentId: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getParentByEmail(studentId)
                ?.catch { e ->
                    Log.e("TAG", "getParentByEmail: $e")
                    getParentMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getParentByEmailSuccess: $data")
                    getParentMutable.value = FirebaseState.Success(data)
                }
        }}

    fun updateParent(updatedParent: Parent){
        viewModelScope.launch(Dispatchers.IO){
            repo.updateParent(updatedParent)
                ?.catch { e ->
                    Log.e("TAG", "updateParent: $e")
                    updateParentMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "updateParentSuccess: $data")
                    updateParentMutable.value = FirebaseState.Success(data)
                }
        }}
    fun getAllExistingStudents(teacher_id: String,semester: String){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllExistingStudents(teacher_id,semester)
                ?.catch { e ->
                    Log.e("TAG", "getAllExistingStudents: $e")
                    getExistingStudentsMutable.value = FirebaseState.Failure(e)
                }
                ?.collect { data ->
                    Log.i("TAG", "getAllExistingStudentsSuccess: $data")
                    getExistingStudentsMutable.value = FirebaseState.Success(data)
                }
        }}

    fun addStudentToNewSemester(updatedStudent: Student){
        viewModelScope.launch(Dispatchers.IO) {
            repo.addStudentToNewSemester(updatedStudent)
                .catch { e ->
                    Log.e("TAG", "addStudentToNewSemester: $e")
                    addStudentToNewSemesterMutable.value = FirebaseState.Failure(e)
                }
                .collect { data ->
                    Log.i("TAG", "addStudentToNewSemesterSuccess: $data")
                    addStudentToNewSemesterMutable.value = FirebaseState.Success(data)
                }
        }
    }
    fun deleteParent(studentId: String, callback: (Boolean) -> Unit){
        viewModelScope.launch {
            repo.deleteParent(studentId, callback)
        }
    }

}
