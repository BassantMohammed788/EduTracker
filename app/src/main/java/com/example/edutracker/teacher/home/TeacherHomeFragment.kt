package com.example.edutracker.teacher.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.Navigation
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentTeacherHomeBinding

class TeacherHomeFragment : Fragment() {

    lateinit var binding:FragmentTeacherHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var spinner = binding.spinner

        val items = arrayOf("Semester 1", "Semester 2", "Semester 3")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.AssistantCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherHomeFragment_to_teacherAllAssistantFragment)
        }
        binding.GroupCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherHomeFragment_to_allGroupsFragment)
        }
    }

}