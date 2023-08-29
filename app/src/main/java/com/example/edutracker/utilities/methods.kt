package com.example.edutracker.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*
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
 fun gradeLevel(targetGrade: String?): String? {

    val gradeLevelsList = listOf(
        "First preparatory Grade",
        "Second preparatory Grade",
        "Third preparatory Grade",
        "First Secondary Grade",
        "Second Secondary Grade",
        "Third Secondary Grade",
        "الصف الاول الاعدادي",
        "الصف الثاني الاعدادي",
        "الصف الثالث الاعدادي",
        "الصف الاول الثانوي",
        "الصف الثاني الثانوي",
        "الصف الثالث الثانوي"
    )
    var result: String? = null
    if (targetGrade == gradeLevelsList[0]||targetGrade == gradeLevelsList[6]){
        result=gradeLevelsList[0]

    }else if (targetGrade == gradeLevelsList[1]||targetGrade == gradeLevelsList[7]){
        result=gradeLevelsList[1]
    }else if (targetGrade == gradeLevelsList[2]||targetGrade == gradeLevelsList[8]){
        result=gradeLevelsList[2]
    }else if (targetGrade == gradeLevelsList[3]||targetGrade == gradeLevelsList[9]){
        result=gradeLevelsList[3]
    }else if (targetGrade == gradeLevelsList[4]||targetGrade == gradeLevelsList[10]){
        result=gradeLevelsList[4]
    }else if (targetGrade == gradeLevelsList[5]||targetGrade == gradeLevelsList[11]){
        result=gradeLevelsList[5]
    }
    return result
}

fun convertEnglishTimeToArabic(timeString: String): String {
    val englishLocale = Locale("en")
    val arabicLocale = Locale("ar")

    val inputFormat = SimpleDateFormat("d MMMM yyyy h:mm a", englishLocale)
    val outputFormat = SimpleDateFormat("d MMMM yyyy h:mm a", arabicLocale)

    val date = inputFormat.parse(timeString)
    return outputFormat.format(date)
}



