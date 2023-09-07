package com.example.edutracker.student

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.edutracker.R
import com.example.edutracker.databinding.ActivityMainBinding
import com.example.edutracker.databinding.ActivityStudentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class StudentActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityStudentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNavBar
        navController = Navigation.findNavController(this, R.id.student_nav_host)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}