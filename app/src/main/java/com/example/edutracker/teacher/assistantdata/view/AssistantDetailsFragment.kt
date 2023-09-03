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
import androidx.navigation.fragment.navArgs
import com.example.edutracker.R
import com.example.edutracker.databinding.AlertDialogBinding
import com.example.edutracker.databinding.FragmentAssistantDetailsBinding
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
import kotlinx.coroutines.launch

class AssistantDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAssistantDetailsBinding
    private lateinit var viewModel: AssistantDataViewModel
    private lateinit var viewModelFactory: AssistantDataViewModelFactory
    private val args: AssistantDetailsFragmentArgs by navArgs()
    private lateinit var assistant: Assistant
    private var teacherId=""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAssistantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory =
            AssistantDataViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[AssistantDataViewModel::class.java]
        binding.assistantEmailETt.isFocusableInTouchMode = false
        assistant=args.assistant
         teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        setDataInEditText(assistant)
        binding.updateButton.setOnClickListener {
            updateAssistant()
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack()
            }
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
    private fun updateAssistant(){
        if (checkConnectivity(requireContext())){
        lifecycleScope.launch {
            val email = binding.assistantEmailETt.text.toString()
            val transformedEmail = email.replace(".", ",")
            val name = binding.assistantNameET.text.toString()
            val pass = binding.assistantPasswordET.text.toString()
            val phone = binding.assistantPhoneET.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && phone.isNotEmpty()) {
                if (isValidEmail(email)) {
                    if (checkEgyptianPhoneNumber(phone)) {
                        val assistant = Assistant(
                            name,
                            transformedEmail,
                            phone,
                            pass,
                            teacherId
                        )
                        if (args.assistant==assistant){
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.no_thing_change),
                                Toast.LENGTH_SHORT
                            ).show()
                        }else {
                            viewModel.updateAssistant(assistant)
                            viewModel.updateAssistant.collect {
                                when (it) {
                                    is FirebaseState.Loading -> {
                                        Log.i("TAG", "onViewCreated: loading update")
                                    }
                                    is FirebaseState.Success -> {
                                        if (it.data) {
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.updated_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Navigation.findNavController(requireView()).apply {
                                                popBackStack() // Clear the back stack up to teacherAllAssistantFragment
                                            }
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
                                    else -> {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.network_lost_title),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                }
                            }
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.phoneNotValid),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.emailNotValid),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.fillAllFields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        }else{
               Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }
}

