package com.example.edutracker.teacher.assistantdata.view

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.edutracker.R
import com.example.edutracker.databinding.AlertDialogBinding
import com.example.edutracker.databinding.FragmentAssistantDetailsBinding
import com.example.edutracker.databinding.FragmentTeacherAllAssistantBinding
import com.example.edutracker.dataclasses.Assistant
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.assistantdata.viewmodel.AssistantDataViewModel
import com.example.edutracker.teacher.assistantdata.viewmodel.AssistantDataViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.example.edutracker.utilities.checkEgyptianPhoneNumber
import com.example.edutracker.utilities.isValidEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssistantDetailsFragment : Fragment() {

    lateinit var binding: FragmentAssistantDetailsBinding
    lateinit var viewModel: AssistantDataViewModel
    lateinit var viewModelFactory: AssistantDataViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAssistantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory =
            AssistantDataViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[AssistantDataViewModel::class.java]
        binding.assistantEmailET.isEnabled = false
        binding.assistantEmailET.isClickable = false
        binding.assistantEmailETt.isEnabled = false
        binding.assistantEmailETt.isFocusable = false
        binding.assistantEmailETt.isFocusableInTouchMode = false
        val email = "bassant,mohammed788@gmail,com"
        val teacher_id = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        if (checkConnectivity(requireContext())) {
            viewModel.getAssistantByEmail(teacher_id, email)
            lifecycleScope.launch {
                viewModel.getAssistantByEmail(
                    teacher_id,
                    email
                )
                viewModel.oneAssistants.collect { result ->
                    when (result) {
                        is FirebaseState.Loading -> {
                            Log.i("TAG", "onViewCreated: loading")
                        }
                        is FirebaseState.Success -> {
                            Log.i("TAG", "onViewCreated: ${result.data}")
                            setDataInEditText(result.data)
                            binding.signUpButton.setOnClickListener {
                                updateAssistant()
                            }
                            binding.cancelButton.setOnClickListener {
                                deleteAssistant()
                            }
                        }
                        is FirebaseState.Failure -> {
                            Log.i("TAG", "onViewCreated: fail")
                        }
                    }
                }
            }
        }else{
            binding.constraint.visibility=View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }
    private fun setDataInEditText(assistant: Assistant){
       val transformedEmail = assistant.email.replace(",", ".")
        binding.assistantEmailETt.setText(transformedEmail)
        binding.assistantEmailET.isEnabled = true
        binding.assistantNameET.setText(assistant.name)
        binding.assistantNameET.isEnabled = true
        binding.assistantPhoneET.setText(assistant.phone)
        binding.assistantPhoneET.isEnabled = true
        binding.assistantPasswordET.setText(assistant.password)
        binding.assistantPasswordET.isEnabled = true
    }
    private fun deleteAssistant(){
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = AlertDialogBinding.inflate(layoutInflater)
        builder.setView(alertDialog.root)
        val dialog = builder.create()
        dialog.show()
        alertDialog.dialogYesBtn.setOnClickListener {
            val transformedEmail =  binding.assistantEmailETt.text.toString().replace(".", ",")
            viewModel.deleteAssistant(MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                transformedEmail)
            Toast.makeText(requireContext(),"deleted successfully",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            val navController = Navigation.findNavController(requireView())
            navController.apply {
                navigate(R.id.action_assistantDetailsFragment_to_teacherAllAssistantFragment)
               // popBackStack() // Clear the back stack up to teacherAllAssistantFragment
            }
        }
        alertDialog.dialogNoBtn.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun updateAssistant(){
        lifecycleScope.launch {
            val email = binding.assistantEmailETt.text.toString()
            val transformedEmail = email.replace(".", ",")
            val name = binding.assistantNameET.text.toString()
            val pass = binding.assistantPasswordET.text.toString()
            val phone = binding.assistantPhoneET.text.toString()
            if (name.isNotEmpty()&&email.isNotEmpty()&&pass.isNotEmpty()&&phone.isNotEmpty()){
                if (isValidEmail(email)){
                    if (checkEgyptianPhoneNumber(phone)){
                        val assistant=Assistant(name,transformedEmail,phone,pass,MySharedPreferences.getInstance(requireContext()).getTeacherID().toString())
                        viewModel.updateAssistant(
                            MySharedPreferences.getInstance(
                                requireContext()
                            ).getTeacherID()!!, transformedEmail, assistant
                        )
                        viewModel.updateAssistant.collect {
                            when (it) {
                                is FirebaseState.Loading -> {
                                    Log.i("TAG", "onViewCreated: loading update")
                                }
                                is FirebaseState.Success -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Navigation.findNavController(requireView())
                                        .navigate(R.id.action_assistantDetailsFragment_to_teacherAllAssistantFragment)
                                }
                                else -> {}
                            }
                        }

                    }else{
                        Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), getString(R.string.emailNotValid), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()

            }
        }
    }
}

