package com.example.edutracker.teacher.payment.view

import GradeLevelAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentPaymentBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.databinding.NoSemesterDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.teacher.lessons.view.GroupAdapter
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModel
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModelFactory
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class PaymentFragment : Fragment() {
    private lateinit var binding:FragmentPaymentBinding
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory
    private lateinit var lessonViewModel: LessonsViewModel
    private lateinit var lessonsViewModelFactory: LessonsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter
    private lateinit var groupsAdapter: GroupAdapter
    private lateinit var monthsAdapter: GradeLevelAdapter
    private var gradeVar: String? = null
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var monthVar: String? = null
    private var teacherIdVar: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        groupsViewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        groupsViewModel = ViewModelProvider(this, groupsViewModelFactory)[GroupsViewModel::class.java]
        lessonsViewModelFactory = LessonsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        lessonViewModel = ViewModelProvider(this, lessonsViewModelFactory)[LessonsViewModel::class.java]

        if (MySharedPreferences.getInstance(requireContext()).getSemester()==null){
            binding.constraintLayout.visibility=View.GONE
            val builder = AlertDialog.Builder(requireContext())
            val noSemester = NoSemesterDialogBinding.inflate(layoutInflater)
            builder.setView(noSemester.root)
            val dialog = builder.create()
            dialog.show()
            noSemester.dialogYesBtn.setOnClickListener {
                Navigation.findNavController(requireView()).popBackStack()
                dialog.dismiss()
            }

        }else{
            teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
            semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!

            gradeLevelAdapter = GradeLevelAdapter(emptyList(),gradeLambda)
            groupsAdapter=GroupAdapter(groupClickLambda)
            monthsAdapter=GradeLevelAdapter (emptyList(),monthClickLambda)
            binding.chooseLevel.setOnClickListener{
                if (semesterVar!=null){
                    displayGradeLevelDialog()
                }else{
                    Toast.makeText(requireContext(), getString(R.string.you_should_choose_semester), Toast.LENGTH_SHORT).show()
                }
            }

            /*  binding.chooseGroup.setOnClickListener{
                  if (gradeVar!=null){
                      displayGroupDialog()
                  }else{
                      Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level), Toast.LENGTH_SHORT).show()
                  }
              }*/
            /* binding.chooseMonth.setOnClickListener {
                 if (gradeVar!=null&&groupNameVar!=null){
                     displayMonthDialog()
                 }else{
                     Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level_and_group), Toast.LENGTH_SHORT).show()
                 }
             }*/

            binding.recordPaymentButton.setOnClickListener{
                if (monthVar!=null) {
                    val action =
                        PaymentFragmentDirections.actionPaymentFragmentToRecordPaymentFragment(groupIdVar!!,monthVar!!,gradeVar!!,groupNameVar!!)
                    Navigation.findNavController(requireView()).navigate(action)
                }else{
                    Toast.makeText(requireContext(),getString(R.string.choose_month), Toast.LENGTH_SHORT).show()
                }

            }

        }


    }
    private fun displayGradeLevelDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = gradeLevelAdapter
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                groupsViewModel.getAllGrades(semesterVar!!, MySharedPreferences.getInstance(requireContext()).getTeacherID()!!)
                groupsViewModel.getGrades.collect{ result->
                    when(result){
                        is FirebaseState.Loading ->{
                            gradeLevelDialog.progressBar.visibility=View.VISIBLE
                            gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                            gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                            gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success ->{
                            if (result.data.isEmpty()){
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE

                                gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.VISIBLE

                                gradeLevelDialog.noDataAnimationView.visibility=View.VISIBLE
                                gradeLevelDialog.noDataTv.text=getString(R.string.no_grades_yet)
                            }else{
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE

                                gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                val list = mutableListOf<String>()
                                for (i in result.data){
                                    list.add(i)
                                }
                                gradeLevelAdapter.setGradeLevelsList(list)
                            }

                        }
                        is FirebaseState.Failure ->{}

                    }

                }
            }
        }else{
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            if (gradeVar!=null) {
                binding.gradeName.text = gradeVar
                displayGroupDialog()
            }
            dialog.dismiss()

        }
    }
    private fun displayGroupDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = groupsAdapter
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_group)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            if (gradeVar != null){
                lifecycleScope.launch {
                    groupsViewModel.getAllGroups(semesterVar!!,MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                        gradeVar!!)
                    groupsViewModel.getGroups.collect{ result->
                        when(result){
                            is FirebaseState.Loading ->{
                                gradeLevelDialog.progressBar.visibility=View.VISIBLE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE
                                gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                            }
                            is FirebaseState.Success ->{
                                if (result.data.isEmpty()){
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                    gradeLevelDialog.noDataTv.text=getString(R.string.no_groups_yet)
                                    gradeLevelDialog.noDataTv.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility=View.VISIBLE
                                }else{
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                    gradeLevelDialog.noDataTv.visibility=View.INVISIBLE

                                    groupsAdapter.submitList(result.data)
                                }

                            }
                            is FirebaseState.Failure ->{}

                        }
                    }
                }
            }else{
                Toast.makeText(requireContext(),getString(R.string.you_should_choose_semester_level),Toast.LENGTH_LONG).show()
            }}else{
            binding.constraintLayout.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            if (groupNameVar!=null) {
                binding.groupName.text = groupNameVar
                displayMonthDialog()
            }
            dialog.dismiss()
        }
    }

    private fun displayMonthDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val monthDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        monthDialog.GradeLevelRecycler.adapter = monthsAdapter
        monthDialog.GradeTv.text=getString(R.string.choose_month)
        builder.setView(monthDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                lessonViewModel.getAllMonths(semesterVar!!, MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                    gradeVar!!,groupIdVar!!)
                lessonViewModel.getAllMonths.collect{ result->
                    when(result){
                        is FirebaseState.Loading ->{
                            monthDialog.progressBar.visibility=View.VISIBLE
                            monthDialog.noDataAnimationView.visibility=View.INVISIBLE
                            monthDialog.noDataTv.visibility=View.INVISIBLE
                            monthDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success ->{
                            if (result.data.isEmpty()){
                                monthDialog.progressBar.visibility=View.INVISIBLE
                                monthDialog.GradeLevelRecycler.visibility=View.GONE
                                monthDialog.noDataTv.visibility=View.VISIBLE
                                monthDialog.noDataAnimationView.visibility=View.VISIBLE
                                monthDialog.noDataTv.text=getString(R.string.no_months_yet)
                            }else{
                                monthDialog.noDataTv.visibility=View.INVISIBLE
                                monthDialog.progressBar.visibility=View.GONE
                                monthDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                monthDialog.noDataAnimationView.visibility=View.INVISIBLE
                                val list = mutableListOf<String>()
                                val arabicMonthList = listOf(
                                    "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                                    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
                                )
                                for (i in result.data){
                                    if (i in arabicMonthList) {
                                        list.add(i)
                                    }
                                }
                                monthsAdapter.setGradeLevelsList(list)
                            }

                        }
                        is FirebaseState.Failure ->{}

                    }

                }
            }
        }else{
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        monthDialog.okBTN.setOnClickListener {
            if (monthVar!=null){
                binding.monthName.text = monthVar
            }
            dialog.dismiss()
        }
    }


    private val gradeLambda = { string: String ->
        gradeVar = string
    }

    private val groupClickLambda = { group: Group ->

        groupNameVar = group.name
        Log.i("TAG", ":$groupNameVar ")
        groupIdVar = group.id
    }
    private val monthClickLambda = { string: String ->

        Log.i("TAG", ":${ string } ")
        monthVar = string

    }


}