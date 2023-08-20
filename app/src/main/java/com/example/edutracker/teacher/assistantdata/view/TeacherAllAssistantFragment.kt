package com.example.edutracker.teacher.assistantdata.view

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
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentTeacherAllAssistantBinding
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.assistantdata.viewmodel.AssistantDataViewModel
import com.example.edutracker.teacher.assistantdata.viewmodel.AssistantDataViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch


class TeacherAllAssistantFragment : Fragment() {
    lateinit var binding: FragmentTeacherAllAssistantBinding
    lateinit var viewModel: AssistantDataViewModel
    lateinit var viewModelFactory: AssistantDataViewModelFactory
    lateinit var recyclerView: RecyclerView
    lateinit var Aadapter:AssistantAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTeacherAllAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = AssistantDataViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[AssistantDataViewModel::class.java]
        Aadapter = AssistantAdapter(cardLambda)
        recyclerView = binding.assistantsRecycler
        recyclerView.apply {
            adapter = Aadapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }


        binding.addAssistantFAB.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherAllAssistantFragment_to_teacherAddNewAssistantFragment)

        }
        if (checkConnectivity(requireContext())){

            lifecycleScope.launch {
                viewModel.getAllAssistants(MySharedPreferences.getInstance(requireContext()).getTeacherID()!!)
                viewModel.getAllAssistants.collect{ result ->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.assistantProgressBar.visibility=View.VISIBLE
                            binding.noAssistantTv.visibility=View.GONE
                            Log.i("TAG", "onViewCreated: loading")
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()){
                                binding.assistantProgressBar.visibility=View.GONE
                                binding.noAssistantTv.visibility=View.VISIBLE
                                Log.i("TAG", "onViewCreated: no assistant yet")
                            }else{
                                Aadapter.submitList(result.data)
                                binding.noAssistantTv.visibility=View.GONE
                                binding.assistantProgressBar.visibility=View.GONE
                                Log.i("TAG", "onViewCreated: ${result.data[0].name}")
                            }
                        }
                        is FirebaseState.Failure -> {
                            binding.noAssistantTv.visibility=View.GONE
                            Log.i("TAG", "onViewCreated: fail")
                        }
                    }
                }
            }

        }else{
            binding.constraintLayout.visibility=View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }

    }
    private val cardLambda = { assistant: Assistant ->
        /*val action = TAAD.actionCategoryFragmentToSubCategoryFragment("",
            categoryID.toString())*/
        Navigation.findNavController(requireView()).navigate(R.id.action_teacherAllAssistantFragment_to_assistantDetailsFragment)
    }
}