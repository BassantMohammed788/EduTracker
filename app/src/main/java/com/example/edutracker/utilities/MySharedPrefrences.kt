package com.example.edutracker.utilities

import android.content.Context

class MySharedPreferences private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: MySharedPreferences? = null

        fun getInstance(context: Context): MySharedPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = MySharedPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val sharedPreferences =
        context.getSharedPreferences(Constants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)

    fun saveUserType(type: String) {
        sharedPreferences.edit().putString(Constants.USERTYPE, type).apply()
    }

    fun getUserType(): String? {
        return sharedPreferences.getString(Constants.USERTYPE, null)
    }
    fun saveTeacherID(id: String) {
        sharedPreferences.edit().putString(Constants.TEACHER_ID, id).apply()
    }

    fun getTeacherID(): String? {
        return sharedPreferences.getString(Constants.TEACHER_ID, null)
    }
    fun saveSemester(type: String?) {
        sharedPreferences.edit().putString(Constants.SEMESTER, type).apply()
    }

    fun getSemester(): String? {
        return sharedPreferences.getString(Constants.SEMESTER, null)
    }
    fun saveTeacherEmail(type: String?) {
        sharedPreferences.edit().putString(Constants.TEACHER_EMAIL, type).apply()
    }

    fun getTeacherEmail(): String? {
        return sharedPreferences.getString(Constants.TEACHER_EMAIL, null)
    }
    fun saveTeacherName(type: String?) {
        sharedPreferences.edit().putString(Constants.TEACHER_NAME, type).apply()
    }

    fun getTeacherName(): String? {
        return sharedPreferences.getString(Constants.TEACHER_NAME, null)
    }
    fun saveTeacherPhone(type: String?) {
        sharedPreferences.edit().putString(Constants.TEACHER_PHONE, type).apply()
    }

    fun getTeacherPhone(): String? {
        return sharedPreferences.getString(Constants.TEACHER_PHONE, null)
    }
    fun saveTeacherSubject(type: String?) {
        sharedPreferences.edit().putString(Constants.TEACHER_SUBJECT, type).apply()
    }

    fun getTeacherSubject(): String? {
        return sharedPreferences.getString(Constants.TEACHER_SUBJECT, null)
    }
    fun saveIsUserLoggedIn(type: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.IS_USER_LOGGED_IN, type).apply()
    }

    fun getIsUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.IS_USER_LOGGED_IN, false)
    }
    fun saveUserName(type: String?) {
        sharedPreferences.edit().putString(Constants.USER_NAME, type).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(Constants.USER_NAME, null)
    }
    fun saveUserPhone(type: String?) {
        sharedPreferences.edit().putString(Constants.USER_PHONE, type).apply()
    }

    fun getUserPhone(): String? {
        return sharedPreferences.getString(Constants.USER_PHONE, null)
    }
    fun saveUserPassword(type: String?) {
        sharedPreferences.edit().putString(Constants.USER_PASSWORD, type).apply()
    }

    fun getUserPassword(): String? {
        return sharedPreferences.getString(Constants.USER_PASSWORD, null)
    }
    fun saveUserEmail(type: String?) {
        sharedPreferences.edit().putString(Constants.USER_EMAIL, type).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(Constants.USER_EMAIL, null)
    }
    fun saveStudentID(type: String?) {
        sharedPreferences.edit().putString(Constants.STUDENT_ID, type).apply()
    }

    fun getStudentID(): String? {
        return sharedPreferences.getString(Constants.STUDENT_ID, null)
    }
    fun saveStudentGradeLevel(type: String?) {
        sharedPreferences.edit().putString(Constants.STUDENT_GRADE_LEVEL, type).apply()
    }

    fun getStudentGradeLevel(): String? {
        return sharedPreferences.getString(Constants.STUDENT_GRADE_LEVEL, null)
    }
    fun saveStudentGroupID(type: String?) {
        sharedPreferences.edit().putString(Constants.STUDENT_GROUP_ID, type).apply()
    }

    fun getStudentGroupID(): String? {
        return sharedPreferences.getString(Constants.STUDENT_GROUP_ID, null)
    }








}