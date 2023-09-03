package com.example.edutracker.teacher.profile.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.edutracker.authentication.AuthenticationActivity
import com.example.edutracker.databinding.FragmentProfileBinding
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.profile.viewmodel.ProfileViewModel
import com.example.edutracker.teacher.profile.viewmodel.ProfileViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var profileViewModelFactory: ProfileViewModelFactory

    private var semesterVar: String? = null
    private var teacherNameVar: String? = null
    private var teacherEmailVar: String? = null
    private var teacherPhoneVar: String? = null
    private var teacherSubjectVar: String? = null
    private var teacherIdVar: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModelFactory = ProfileViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        profileViewModel = ViewModelProvider(this, profileViewModelFactory)[ProfileViewModel::class.java]


        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!

        teacherNameVar = MySharedPreferences.getInstance(requireContext()).getTeacherName()
        teacherEmailVar = MySharedPreferences.getInstance(requireContext()).getTeacherEmail()
        teacherPhoneVar = MySharedPreferences.getInstance(requireContext()).getTeacherPhone()
        teacherSubjectVar = MySharedPreferences.getInstance(requireContext()).getTeacherSubject()

        /*binding.emailTv.text=teacherEmailVar
        binding.subjectTv.text=teacherSubjectVar
        binding.phoneTv.text=teacherPhoneVar
        binding.name.text=teacherNameVar*/

        binding.logoutButton.setOnClickListener {
            MySharedPreferences.getInstance(requireContext()).saveIsUserLoggedIn(false)
            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        lifecycleScope.launch {
            if (checkConnectivity(requireContext())){
                profileViewModel.getGradesAndGroupsCounts(semesterVar!!,teacherIdVar!!)
                profileViewModel.getGradeAndGroupCounts.collect{ result->
                    when(result){
                        is FirebaseState.Success->{
                            binding.gradeLevelsCount.text=result.data.first.toString()
                            binding.groupsCount.text=result.data.second.toString()
                        }
                        else->{}
                    }
                }

            }
        }

    }

}