package com.example.edutracker.teacher.exams.view

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
import com.example.edutracker.databinding.FragmentAllExamsBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.databinding.NoSemesterDialogBinding
import com.example.edutracker.dataclasses.Exam
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.exams.viewmodel.ExamsViewModel
import com.example.edutracker.teacher.exams.viewmodel.ExamsViewModelFactory
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.teacher.lessons.view.GroupAdapter
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch

class AllExamsFragment : Fragment() {
    lateinit var binding:FragmentAllExamsBinding
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory

    private lateinit var examViewModel: ExamsViewModel
    private lateinit var examViewModelFactory: ExamsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter

    private lateinit var examAdapter: ExamsAdapter
    private var gradeVar: String? = null
    private lateinit var groupsAdapter: GroupAdapter
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var teacherIdVar: String? = null

    private var examNameVar: String? = null
    private lateinit var examObjVar: Exam


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAllExamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupsViewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        groupsViewModel = ViewModelProvider(this, groupsViewModelFactory)[GroupsViewModel::class.java]

        examViewModelFactory = ExamsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        examViewModel = ViewModelProvider(this, examViewModelFactory)[ExamsViewModel::class.java]

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
        }else {
            teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
            semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!

            gradeLevelAdapter = GradeLevelAdapter(emptyList(),gradeLambda)
            groupsAdapter=GroupAdapter(groupClickLambda)
            examAdapter=ExamsAdapter(examLambda)

            binding.recordPaymentButton.setOnClickListener {
                if (examNameVar!=null){
                    val action =
                        AllExamsFragmentDirections.actionAllExamsFragmentToRecordExamDegreesFragment(groupIdVar!!,groupNameVar!!,gradeVar!!,examObjVar)
                    Navigation.findNavController(requireView()).navigate(action)
                }else{
                    Toast.makeText(requireContext(),getString(R.string.choose_exam), Toast.LENGTH_SHORT).show()
                }
            }

            binding.chooseLevel.setOnClickListener{
                if (semesterVar!=null){
                    displayGradeLevelDialog()
                }else{
                    Toast.makeText(requireContext(), getString(R.string.you_should_choose_semester), Toast.LENGTH_SHORT).show()
                }
            }
            binding.addExamFAB.setOnClickListener {
                Navigation.findNavController(requireView()).navigate(R.id.action_allExamsFragment_to_addExamFragment)
            }
            binding.chooseGroup.setOnClickListener{
                Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level), Toast.LENGTH_SHORT).show()

                /*  if (gradeVar!=null){
                        displayGroupDialog()
                    }else{
                        Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level), Toast.LENGTH_SHORT).show()
                    }*/
            }
            binding.chooseExam.setOnClickListener{
                Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level_and_group), Toast.LENGTH_SHORT).show()

                /*  if(groupIdVar!=null){
                      displayExamsDialog()
                  }else{
                      Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level_and_group), Toast.LENGTH_SHORT).show()
                  }*/
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
                            gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE

                            gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                            gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success ->{
                            if (result.data.isEmpty()){
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE

                                gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataAnimationView.visibility=View.VISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.VISIBLE
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
                displayExamsDialog()
            }
            dialog.dismiss()
        }
    }
    private fun displayExamsDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = examAdapter
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_exam)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            if (groupIdVar != null){
                lifecycleScope.launch {
                    examViewModel.getAllExams(teacherIdVar!!,semesterVar!!,gradeVar!!,groupIdVar!!)
                    examViewModel.getAllExams.collect{ result->
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
                                    gradeLevelDialog.noDataTv.text=getString(R.string.no_exams_added_to_this_group)
                                    gradeLevelDialog.noDataTv.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility=View.VISIBLE
                                }else{
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                    gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                    examAdapter.submitList(result.data)
                                }

                            }
                            is FirebaseState.Failure ->{}

                        }
                    }
                }
            }else{
                Toast.makeText(requireContext(),getString(R.string.you_should_choose_exam),Toast.LENGTH_LONG).show()
            }}else{
            binding.constraintLayout.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            if (examNameVar!=null) {
                binding.examName.text = examNameVar
            }
            dialog.dismiss()
        }
    }
    private val gradeLambda = { string: String ->
        gradeVar = string
    }

    private val examLambda = { exam: Exam ->
        examNameVar=exam.examName
        examObjVar=exam
    }
    private val groupClickLambda = { group: Group ->

        groupNameVar = group.name
        Log.i("TAG", ":$groupNameVar ")
        groupIdVar = group.id
    }
}