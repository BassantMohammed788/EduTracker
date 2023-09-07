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
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import kotlinx.coroutines.launch
import java.util.*

class AddNewGroupFragment : Fragment() {

    private lateinit var binding: FragmentAddNewGroupBinding
    private lateinit var viewModel: GroupsViewModel
    private lateinit var viewModelFactory: GroupsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter
    private var gradeVar:String?= null
    private lateinit var gradeLevelsArray : List<String>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        val teacherId =MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        val semester = MySharedPreferences.getInstance(requireContext()).getSemester()!!
        binding.chooseLevel.setOnClickListener{
            displayGradeLevelDialog()
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack() // Clear the back stack up to teacherAllAssistantFragment
            }
        }
        binding.AddButton.setOnClickListener {
          if (gradeVar!=null&&binding.groupNameET.text.isNotEmpty()){
              lifecycleScope.launch {
                  val  gradeLevel = gradeVar!!
                  val groupName= binding.groupNameET.text.toString()
                  val group = Group(teacherId,groupName, UUID.randomUUID().toString(),gradeLevel)
                  val builder = AlertDialog.Builder(requireContext())
                  val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
                  builder.setView(loadingDialogB.root)
                  val dialog = builder.create()
                  dialog.setCancelable(false)
                  viewModel.addGroup(group,teacherId,semester)
                  viewModel.addGroup.collect{ result->
                      when (result) {
                          is FirebaseState.Loading -> {
                              Log.i("TAG", "onViewCreated: loading")
                              dialog.show()
                          }
                          is FirebaseState.Success -> {
                              dialog.dismiss()
                              if (result.data) {
                                  Toast.makeText(requireContext(), getString(R.string.added_successfully), Toast.LENGTH_SHORT).show()
                                  Navigation.findNavController(requireView()).apply {
                                      popBackStack()
                                  }
                                   } else {
                                  Toast.makeText(requireContext(), "The Group already exists", Toast.LENGTH_SHORT).show()
                              }
                          }
                          is FirebaseState.Failure -> {
                              dialog.dismiss()
                              binding.groupProgressBar.visibility=View.GONE
                              Toast.makeText(requireContext(), "Error: ${result.msg}", Toast.LENGTH_SHORT).show()
                          }
                      }
                  }
              }
          }else{
              Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level_and_group), Toast.LENGTH_SHORT).show()
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
            if (gradeVar!=null){
            binding.gradeName.text = gradeVar}
            dialog.dismiss()

        }
    }
    private val gradeLambda = { string: String ->
        gradeVar = string
    }
    /* public fun gradeLevel(targetGrade: String): String? {

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
    }*/

}