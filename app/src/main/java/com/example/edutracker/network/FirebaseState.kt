package com.example.edutracker.network

sealed class FirebaseState<out T> {
    class Success<T>(val data: T) : FirebaseState<T>()
    class Failure(val msg: Throwable) : FirebaseState<Nothing>()
    object Loading : FirebaseState<Nothing>()
}
