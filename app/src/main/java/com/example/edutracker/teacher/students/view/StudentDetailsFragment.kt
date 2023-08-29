package com.example.edutracker.teacher.students.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.edutracker.databinding.FragmentStudentDetailsBinding

class StudentDetailsFragment : Fragment() {
    lateinit var binding:FragmentStudentDetailsBinding
    private val args: StudentDetailsFragmentArgs by navArgs()
    private var studentId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentId = args.studentId
    }
}