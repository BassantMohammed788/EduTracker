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








}