package com.example.edutracker.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.edutracker.teacher.MainActivity
import com.example.edutracker.R
import com.example.edutracker.authentication.view.AuthenticationActivity
import com.example.edutracker.student.StudentActivity
import com.example.edutracker.utilities.Constants
import com.example.edutracker.utilities.MySharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        GlobalScope.launch(Dispatchers.Main) {
            delay(3000)
            if (MySharedPreferences.getInstance(this@SplashActivity).getIsUserLoggedIn()) {
                if (MySharedPreferences.getInstance(this@SplashActivity).getUserType() == Constants.TEACHER || MySharedPreferences.getInstance(this@SplashActivity).getUserType() == Constants.ASSISTANT){
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }else if (MySharedPreferences.getInstance(this@SplashActivity).getUserType() == Constants.PARENT || MySharedPreferences.getInstance(this@SplashActivity).getUserType() == Constants.STUDENT){
                    val intent = Intent(this@SplashActivity, StudentActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }else{
                val intent = Intent(this@SplashActivity, AuthenticationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        }
}