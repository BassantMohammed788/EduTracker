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
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.AlertDialogBinding
import com.example.edutracker.databinding.FragmentAllGroupsBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch

class AllGroupsFragment : Fragment() {
    lateinit var binding: FragmentAllGroupsBinding
    lateinit var viewModel: GroupsViewModel
    private lateinit var viewModelFactory: GroupsViewModelFactory
    lateinit var gradeLevelAdapter: GradeLevelAdapter
    private var gradeVar:String?= null
    private lateinit var groupsAdapter: GroupsAdapter
    lateinit var recyclerView: RecyclerView
    var semester : String = ""
    var teacherId :String?=null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAllGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupsViewModel::class.java]
        gradeLevelAdapter = GradeLevelAdapter(emptyList() ,gradeLambda)
        binding.groupProgressBar.visibility=View.GONE
        binding.groupsRecycler.visibility=View.INVISIBLE
        binding.noGroupsTv.visibility=View.GONE
         teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
         semester = MySharedPreferences.getInstance(requireContext()).getSemester()!!
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
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_grade_level)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                viewModel.getAllGrades(semester, teacherId!! )
                viewModel.getGrades.collect{ result->
                    when(result){
                        is FirebaseState.Loading ->{
                            gradeLevelDialog.progressBar.visibility=View.VISIBLE
                            gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success ->{
                            if (result.data.isEmpty()){
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.VISIBLE
                                gradeLevelDialog.noDataTv.text=getString(R.string.no_grades_yet)
                            }else{
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE
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
            }
            dialog.dismiss()
            if (checkConnectivity(requireContext())){
                binding.constraintLayout.visibility = View.VISIBLE
                if (gradeVar!= null){
                    lifecycleScope.launch {
                        viewModel.getAllGroups(semester,MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,gradeVar!!)
                        viewModel.getGroups.collect{ result->
                            when(result){
                                is FirebaseState.Loading ->{
                                    binding.groupProgressBar.visibility=View.VISIBLE
                                    binding.groupsRecycler.visibility=View.INVISIBLE
                                    binding.noGroupsTv.visibility=View.GONE
                                }
                                is FirebaseState.Success ->{
                                    if (result.data.isEmpty()){
                                        binding.groupProgressBar.visibility=View.GONE
                                        binding.groupsRecycler.visibility=View.INVISIBLE
                                        binding.noGroupsTv.visibility=View.VISIBLE
                                    }else{
                                        binding.groupProgressBar.visibility=View.GONE
                                        binding.groupsRecycler.visibility=View.VISIBLE
                                        binding.noGroupsTv.visibility=View.GONE
                                        groupsAdapter.gradeLevel=gradeVar
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
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = AlertDialogBinding.inflate(layoutInflater)
        builder.setView(alertDialog.root)
        val dialog = builder.create()
        dialog.show()
        alertDialog.dialogYesBtn.setOnClickListener {
            lifecycleScope.launch {
                lifecycleScope.launch {
                    viewModel.deleteGroup(semester, group.teacherId ,group.gradeLevel,group.id)
                }
            }
            dialog.dismiss()
        }
        alertDialog.dialogNoBtn.setOnClickListener {
            dialog.dismiss()
        }
    }


}