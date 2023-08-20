package com.example.edutracker.teacher.groups.view

import GradeLevelAdapter
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentAllGroupsBinding
import com.example.edutracker.databinding.FragmentTeacherAllAssistantBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AllGroupsFragment : Fragment() {
    lateinit var binding: FragmentAllGroupsBinding
    lateinit var viewModel: GroupsViewModel
    lateinit var viewModelFactory: GroupsViewModelFactory
    lateinit var gradeLevelAdapter: GradeLevelAdapter
    private var gradeVar:String?= null
    lateinit var gradeLevelsArray : List<String>
    lateinit var groupsAdapter: GroupsAdapter
    lateinit var recyclerView: RecyclerView
    var semester : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupsViewModel::class.java]
        gradeLevelsArray = resources.getStringArray(R.array.grade_levels).toList()
        gradeLevelAdapter = GradeLevelAdapter(gradeLevelsArray,gradeLambda)
        binding.groupProgressBar.visibility=View.GONE
        binding.groupsRecycler.visibility=View.GONE
        binding.noGroupsTv.visibility=View.GONE
        val teacher_id = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
         semester = "2023-2024 الترم الاول"
        groupsAdapter=GroupsAdapter(deleteGroupLambda)
        recyclerView = binding.groupsRecycler
        recyclerView.apply {
            adapter = groupsAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        binding.chooseLevel.setOnClickListener{
            displayGradeLevelDialog()
        }
        binding.addGroupFAB.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_allGroupsFragment_to_addNewGroupFragment)
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
            if (checkConnectivity(requireContext())){
                binding.constraintLayout.visibility = View.VISIBLE
                if (gradeLevel(gradeVar)!= null && semester != null){
                    lifecycleScope.launch {
                        viewModel.getAllGroups(semester,MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,gradeLevel(gradeVar)!!)
                        viewModel.getGroups.collect{ result->
                            when(result){
                                is FirebaseState.Loading ->{
                                    binding.groupProgressBar.visibility=View.VISIBLE
                                    binding.groupsRecycler.visibility=View.GONE
                                    binding.noGroupsTv.visibility=View.GONE
                                }
                                is FirebaseState.Success ->{
                                    if (result.data.isEmpty()){
                                        binding.groupProgressBar.visibility=View.GONE
                                        binding.groupsRecycler.visibility=View.GONE
                                        binding.noGroupsTv.visibility=View.VISIBLE
                                    }else{
                                        binding.groupProgressBar.visibility=View.GONE
                                        binding.groupsRecycler.visibility=View.VISIBLE
                                        binding.noGroupsTv.visibility=View.GONE
                                        groupsAdapter.gradeLevel=gradeLevel(gradeVar)
                                        groupsAdapter.submitList(result.data)
                                    }

                                }
                                is FirebaseState.Failure ->{}

                            }
                        }
                    }
                }else{
                    Toast.makeText(requireContext(),"You should choose semester and group",Toast.LENGTH_LONG).show()
                }}else{
                binding.constraintLayout.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
            }

        }
    }
    private val gradeLambda = { string: String ->
        gradeVar = string
    }
    private val deleteGroupLambda = { group: Group ->
        viewModel.deleteGroup(semester, group.teacherId ,group.gradeLevel,group.id)
    }
    public fun gradeLevel(targetGrade: String?): String? {

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