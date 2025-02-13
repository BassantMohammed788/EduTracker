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
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentTeacherAddNewAssistantBinding
import com.example.edutracker.databinding.LoadingDialogBinding
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


class TeacherAddNewAssistantFragment : Fragment() {

    private lateinit var binding: FragmentTeacherAddNewAssistantBinding

   private lateinit var viewModel: AssistantDataViewModel
    private lateinit var viewModelFactory: AssistantDataViewModelFactory


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTeacherAddNewAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = AssistantDataViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[AssistantDataViewModel::class.java]

        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherAddNewAssistantFragment_to_teacherAllAssistantFragment)
        }


        binding.AddAssistantButton.setOnClickListener {
            val name =binding.assistantNameET.text.toString()
            val email=binding.assistantEmailET.text.toString()
            val password = binding.assistantPasswordET.text.toString()
            val phone = binding.assistantPhoneNumberET.text.toString()
            val teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
            val transformedEmail = email.replace(".", ",")
            val builder = AlertDialog.Builder(requireContext())
            val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
            builder.setView(loadingDialogB.root)
            val dialog = builder.create()
            dialog.setCancelable(false)
                dialog.show()
          if (checkConnectivity(requireContext())){
              if (name.isNotEmpty()&&email.isNotEmpty()&&password.isNotEmpty()&&phone.isNotEmpty()){
                  if (isValidEmail(email)){
                      if (checkEgyptianPhoneNumber(phone)){
                          val assistant=Assistant(name,transformedEmail,phone,password,teacherId)
                          lifecycleScope.launch {
                              viewModel.addAssistant(assistant,teacherId){ result->
                                  if (result){
                                      dialog.dismiss()
                                      Toast.makeText(requireContext(), getString(R.string.added_successfully), Toast.LENGTH_SHORT).show()
                                      Navigation.findNavController(requireView()).apply {
                                          popBackStack()
                                      }
                                  }else{
                                      dialog.dismiss()
                                      Toast.makeText(requireContext(), getString(R.string.The_email_already_exists), Toast.LENGTH_SHORT).show()
                                  }
                              }
                          }

                      }else{
                          dialog.dismiss()
                          Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                      }
                  }else{
                      dialog.dismiss()
                      Toast.makeText(requireContext(), getString(R.string.emailNotValid), Toast.LENGTH_SHORT).show()
                  }
              }else{
                  Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()

                  dialog.dismiss()
              }
          }else{
              Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

              dialog.dismiss()
          }


        }



    }

}