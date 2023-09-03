package com.example.edutracker.teacher.assistantdata.view

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.AlertDialogBinding
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
    private lateinit var binding: FragmentTeacherAllAssistantBinding
    private lateinit var viewModel: AssistantDataViewModel
    private lateinit var viewModelFactory: AssistantDataViewModelFactory
    private lateinit var recyclerView: RecyclerView
    private lateinit var assistantAdapter:AssistantAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTeacherAllAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = AssistantDataViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[AssistantDataViewModel::class.java]
        assistantAdapter = AssistantAdapter(cardLambda,deleteLambda)
        recyclerView = binding.assistantsRecycler
        recyclerView.apply {
            adapter = assistantAdapter
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
                                assistantAdapter.submitList(result.data)
                                recyclerView.visibility=View.VISIBLE
                                binding.noAssistantTv.visibility=View.GONE
                                binding.noAssistantTv.text=result.data[0].name
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
        val action = TeacherAllAssistantFragmentDirections.actionTeacherAllAssistantFragmentToAssistantDetailsFragment(assistant)
        Navigation.findNavController(requireView()).navigate(action)
    }
    private val deleteLambda={ assistant : Assistant->
            val builder = AlertDialog.Builder(requireContext())
            val alertDialog = AlertDialogBinding.inflate(layoutInflater)
            builder.setView(alertDialog.root)
            val dialog = builder.create()
            dialog.show()
            alertDialog.dialogYesBtn.setOnClickListener {
                if (checkConnectivity(requireContext())) {
                    viewModel.deleteAssistant(
                        MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                        assistant.email
                    )
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.network_lost_title),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                    dialog.dismiss()
            }
            alertDialog.dialogNoBtn.setOnClickListener {
                dialog.dismiss()
            }

    }
}