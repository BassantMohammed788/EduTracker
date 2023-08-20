package com.example.edutracker.teacher.groups.view

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
import com.example.edutracker.databinding.FragmentAddNewGroupBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.assistantdata.view.AssistantAdapter
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import kotlinx.coroutines.launch
import java.util.*

class AddNewGroupFragment : Fragment() {

    lateinit var binding: FragmentAddNewGroupBinding
    lateinit var viewModel: GroupsViewModel
    lateinit var viewModelFactory: GroupsViewModelFactory
    lateinit var gradeLevelAdapter: GradeLevelAdapter
    private var gradeVar:String?= null
    lateinit var gradeLevelsArray : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddNewGroupBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupsViewModel::class.java]
        gradeLevelsArray = resources.getStringArray(R.array.grade_levels).toList()
        gradeLevelAdapter = GradeLevelAdapter(gradeLevelsArray,gradeLambda)
        binding.groupProgressBar.visibility=View.GONE
        val teacher_id =MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        val semester = "2023-2024 الترم الاول"
        binding.chooseLevel.setOnClickListener{
            displayGradeLevelDialog()
        }
        binding.AddButton.setOnClickListener {
          if (gradeVar!=null&&binding.groupNameET.text.isNotEmpty()){
              lifecycleScope.launch {
                  val  gradeLevel = gradeLevel(gradeVar!!)!!
                  val groupName= binding.groupNameET.text.toString()
                  val group = Group(teacher_id,groupName, UUID.randomUUID().toString(),gradeLevel)
                  viewModel.addGroup(group,teacher_id,semester)
                  viewModel.addGroup.collect{ result->
                      when (result) {
                          is FirebaseState.Loading -> {
                              Log.i("TAG", "onViewCreated: loading")
                              binding.groupProgressBar.visibility=View.VISIBLE
                          }
                          is FirebaseState.Success -> {
                              binding.groupProgressBar.visibility=View.GONE
                              if (result.data) {
                                  Toast.makeText(requireContext(), "Added Successfully", Toast.LENGTH_SHORT).show()
                                  Navigation.findNavController(requireView()).navigate(R.id.action_addNewGroupFragment_to_allGroupsFragment)
                              } else {
                                  Toast.makeText(requireContext(), "The Group already exists", Toast.LENGTH_SHORT).show()
                              }
                          }
                          is FirebaseState.Failure -> {
                              binding.groupProgressBar.visibility=View.GONE
                              Toast.makeText(requireContext(), "Error: ${result.msg}", Toast.LENGTH_SHORT).show()
                          }
                      }
                  }
              }
          }else{
              Toast.makeText(requireContext(), "You must Choose the Grade Level and Group Name", Toast.LENGTH_SHORT).show()
              binding.groupProgressBar.visibility=View.GONE
          }



        }
    }
     fun displayGradeLevelDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = gradeLevelAdapter
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        gradeLevelDialog.okBTN.setOnClickListener {
            binding.gradeName.text = gradeVar
            dialog.dismiss()

        }
    }
    private val gradeLambda = { string: String ->
        gradeVar = string
    }
     public fun gradeLevel(targetGrade: String): String? {

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

}