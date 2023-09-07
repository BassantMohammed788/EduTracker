package com.example.edutracker.teacher.students.view

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
import androidx.navigation.fragment.navArgs
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentParentBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Parent
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.example.edutracker.utilities.checkEgyptianPhoneNumber
import kotlinx.coroutines.launch


class ParentFragment : Fragment() {
    private lateinit var binding:FragmentParentBinding
    private val args: ParentFragmentArgs by navArgs()
    private var studentId = ""
    private var studentName=""
    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private var teacherIdVar : String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]
        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()
        studentId=args.studentId
        studentName=args.studentName

        binding.parentEmailTF.isFocusableInTouchMode = false

        binding.addParentFAB.setOnClickListener {
            val action = ParentFragmentDirections.actionParentFragmentToAddParentFragment(studentId, studentName)
            Navigation.findNavController(requireView()).navigate(action)
        }

        lifecycleScope.launch {
            if (checkConnectivity(requireContext())) {
                studentsViewModel.getParentByEmail(studentId)
                studentsViewModel.getParent.collect {result->
                    when (result) {
                        is FirebaseState.Loading -> {
                            binding.ProgressBar.visibility = View.VISIBLE
                            binding.noPArentTv.visibility=View.INVISIBLE
                            binding.noDataAnimationView.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success -> {
                            if (result.data!=null){
                                binding.editTextsLinear.visibility=View.VISIBLE
                                binding.parentEmailET.isFocusableInTouchMode = false
                                binding.btnLiniar.visibility=View.VISIBLE
                                binding.parentEmailTF.setText(result.data.parentEmail.replace(",", "."))
                                binding.parentPasswordET.setText(result.data.parentPassword)
                                binding.parentPhoneET.setText(result.data.parentPhone)
                                binding.ProgressBar.visibility = View.INVISIBLE

                                binding.noPArentTv.visibility=View.INVISIBLE
                                binding.noDataAnimationView.visibility=View.INVISIBLE

                                binding.cancelButton.setOnClickListener {
                                    Navigation.findNavController(requireView()).apply {
                                        popBackStack()
                                    }
                                }
                                binding.updateButton.setOnClickListener {
                                    updateParent(result.data)
                                }
                            }else{
                                binding.ProgressBar.visibility = View.INVISIBLE
                                binding.noPArentTv.visibility=View.VISIBLE
                                binding.noDataAnimationView.visibility=View.VISIBLE
                                binding.addParentFAB.visibility=View.VISIBLE
                            }
                        }
                        is FirebaseState.Failure -> {
                            binding.ProgressBar.visibility = View.INVISIBLE
                            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            }
            else{
                binding.ProgressBar.visibility=View.INVISIBLE
                Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun updateParent( parent: Parent){
        val builder = AlertDialog.Builder(requireContext())
        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
        builder.setView(loadingDialogB.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                val password = binding.parentPasswordET.text.toString()
                val phone = binding.parentPhoneET.text.toString()
                if (password.isNotEmpty()&&phone.isNotEmpty()){
                        if (checkEgyptianPhoneNumber(phone)){
                            val updatedParent= Parent(parent.parentEmail,password,phone,studentId,teacherIdVar!!)
                            if (parent==updatedParent){
                                  Toast.makeText(requireContext(), getString(R.string.no_thing_change), Toast.LENGTH_SHORT).show()
                              }else{
                                studentsViewModel.updateParent(updatedParent)
                                  studentsViewModel.updateParent.collect{
                                         when (it) {
                                              is FirebaseState.Loading -> {
                                              dialog.show()
                                              }
                                              is FirebaseState.Success -> {
                                                  if (it.data){
                                                      dialog.dismiss()
                                                  Toast.makeText(requireContext(), getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show()
                                                  Navigation.findNavController(requireView()).apply {
                                                      popBackStack()
                                                  }
                                                  }else{
                                                      dialog.dismiss()
                                                      Toast.makeText(
                                                          requireContext(),
                                                          "error",
                                                          Toast.LENGTH_SHORT
                                                      ).show()
                                                  }
                                              }
                                              else -> {
                                                  dialog.dismiss()
                                                  Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
                                              }
                                          }
                                      }
                              }


                        }else{
                            Toast.makeText(requireContext(), getString(R.string.phoneNotValid), Toast.LENGTH_SHORT).show()
                        }

                }else{
                    Toast.makeText(requireContext(), getString(R.string.fillAllFields), Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
    }

}