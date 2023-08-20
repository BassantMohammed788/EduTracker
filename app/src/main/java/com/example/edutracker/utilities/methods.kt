package com.example.edutracker.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.util.regex.Pattern

fun checkConnectivity(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var networkCapabilities: NetworkCapabilities? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }
    return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
fun checkPasswordMatch(password: String, confirmPassword: String):Boolean {
    return password == confirmPassword
}
fun checkEgyptianPhoneNumber(phoneNumber: String): Boolean {
    val egyptianPhoneRegex = Regex("^(01[0-2]|010|015)[0-9]{8}$")
    return egyptianPhoneRegex.matches(phoneNumber)
}

 fun isValidEmail(email: String): Boolean {
    val pattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
    val matcher = pattern.matcher(email)
    return matcher.matches()

}