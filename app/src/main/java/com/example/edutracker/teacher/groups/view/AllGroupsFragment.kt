package com.example.edutracker.teacher.groups.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentAllGroupsBinding
import com.example.edutracker.databinding.FragmentTeacherAllAssistantBinding

class AllGroupsFragment : Fragment() {
    lateinit var binding: FragmentAllGroupsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addGroupFAB.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_allGroupsFragment_to_addNewGroupFragment)
        }
    }


}