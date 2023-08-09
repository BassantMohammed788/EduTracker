package com.example.edutracker.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.edutracker.R

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        val navController: NavController = Navigation.findNavController(this , R.id.auth_nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this,navController)
    }
}