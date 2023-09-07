package com.example.edutracker.teacher.home

import GradeLevelAdapter
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.edutracker.R
import com.example.edutracker.databinding.FragmentTeacherHomeBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.utilities.Constants
import com.example.edutracker.utilities.MySharedPreferences

class TeacherHomeFragment : Fragment() {

    private lateinit var semesterAdapter:GradeLevelAdapter
    lateinit var binding:FragmentTeacherHomeBinding
    private var semesterVar : String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        semesterAdapter = GradeLevelAdapter(emptyList(),semesterLambda)

        binding.StudentCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherHomeFragment_to_allStudentsFragment)

        }
        binding.AssistantCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherHomeFragment_to_teacherAllAssistantFragment)
        }
        binding.GroupCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherHomeFragment_to_allGroupsFragment)
        }
        binding.lessonsCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_teacherHomeFragment_to_allLessonsFragment)
        }
        if (MySharedPreferences.getInstance(requireContext()).getUserType()==Constants.TEACHER){
            if (MySharedPreferences.getInstance(requireContext()).getSemester()!=null){
                binding.AssistantCard.visibility=View.VISIBLE
                binding.StudentCard.visibility=View.VISIBLE
                binding.GroupCard.visibility=View.VISIBLE
                binding.lessonsCard.visibility=View.VISIBLE
            }else{
                binding.AssistantCard.visibility=View.INVISIBLE
                binding.StudentCard.visibility=View.INVISIBLE
                binding.GroupCard.visibility=View.INVISIBLE
                binding.lessonsCard.visibility=View.INVISIBLE
            }

        }else{
            if (MySharedPreferences.getInstance(requireContext()).getSemester()!=null){
                binding.AssistantCard.visibility=View.GONE
                binding.StudentCard.visibility=View.VISIBLE
                binding.GroupCard.visibility=View.VISIBLE
                binding.lessonsCard.visibility=View.VISIBLE
            }else{
                binding.AssistantCard.visibility=View.GONE
                binding.StudentCard.visibility=View.INVISIBLE
                binding.GroupCard.visibility=View.INVISIBLE
                binding.lessonsCard.visibility=View.INVISIBLE
            }
        }


        binding.chooseSemester.setOnClickListener {
            displaySemesterDialog()
        }
    }


    private val semesterLambda = { string: String ->
        semesterVar = string
    }
    private fun retrieveSemestersList():List<String>{
    /*    val year1 = "2023-2024 First Semester"
        val year2 = "2023-2024 Second Semester"
        val year3 = "2024-2025 First Semester"
        val year4 =  "2024-2025 Second Semester"
        val year5 =  "2025-2026 First Semester"
        val year6 = "2025-2026 Second Semester"
        val year7 = "2026-2027 First Semester"
        val year8 = "2026-2027 Second Semester"
        val year9 ="2027-2028 First Semester"
        val year10 = "2027-2028 Second Semester"
        val year11 = "2028-2029 First Semester"
        val year12 = "2028-2029 Second Semester"
        val year13 ="2029-2030 First Semester"
        val year14 = "2029-2030 Second Semester"
        val year15 ="2030-2031 First Semester"
        val year16 ="2030-2031 Second Semester"
        val year17 = "2031-2032 First Semester"
        val year18 = "2031-2032 Second Semester"
        val year19 = "2032-2033 First Semester"
        val year20 = "2032-2033 Second Semester"*/
        val year1 = "الفصل الدراسي الأول ٢٠٢٣-٢٠٢٤"
        val year2 = "الفصل الدراسي الثاني ٢٠٢٣-٢٠٢٤"
        val year3 = "الفصل الدراسي الأول ٢٠٢٤-٢٠٢٥"
        val year4 = "الفصل الدراسي الثاني ٢٠٢٤-٢٠٢٥"
        val year5 = "الفصل الدراسي الأول ٢٠٢٥-٢٠٢٦"
        val year6 = "الفصل الدراسي الثاني ٢٠٢٥-٢٠٢٦"
        val year7 = "الفصل الدراسي الأول ٢٠٢٦-٢٠٢٧"
        val year8 = "الفصل الدراسي الثاني ٢٠٢٦-٢٠٢٧"
        val year9 = "الفصل الدراسي الأول ٢٠٢٧-٢٠٢٨"
        val year10 = "الفصل الدراسي الثاني ٢٠٢٧-٢٠٢٨"
        val year11 = "الفصل الدراسي الأول ٢٠٢٨-٢٠٢٩"
        val year12 = "الفصل الدراسي الثاني ٢٠٢٨-٢٠٢٩"
        val year13 = "الفصل الدراسي الأول ٢٠٢٩-٢٠٣٠"
        val year14 = "الفصل الدراسي الثاني ٢٠٢٩-٢٠٣٠"
        val year15 = "الفصل الدراسي الأول ٢٠٣٠-٢٠٣١"
        val year16 = "الفصل الدراسي الثاني ٢٠٣٠-٢٠٣١"
        val year17 = "الفصل الدراسي الأول ٢٠٣١-٢٠٣٢"
        val year18 = "الفصل الدراسي الثاني ٢٠٣١-٢٠٣٢"
        val year19 = "الفصل الدراسي الأول ٢٠٣٢-٢٠٣٣"
        val year20 = "الفصل الدراسي الثاني ٢٠٣٢-٢٠٣٣"
        return listOf(year1,year2,year3,year4,year5,year6,year7,year8,year9,year10,year11,year12,year13,year14,year15,year16,year17,year18,year19,year20)
    }


    private fun displaySemesterDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = semesterAdapter
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_semester)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        semesterAdapter.setGradeLevelsList(retrieveSemestersList())
        gradeLevelDialog.okBTN.setOnClickListener {
            if (semesterVar!=null){
                 MySharedPreferences.getInstance(requireContext()).saveSemester(semesterVar!!)
                binding.semesterName.text = semesterVar
                if (MySharedPreferences.getInstance(requireContext()).getUserType()==Constants.TEACHER){
                    binding.AssistantCard.visibility = View.VISIBLE
                }else{
                    binding.AssistantCard.visibility = View.GONE
                }
                binding.StudentCard.visibility = View.VISIBLE
                binding.GroupCard.visibility = View.VISIBLE
                binding.lessonsCard.visibility = View.VISIBLE
                dialog.dismiss()
            }else{
                Toast.makeText(requireContext(),getString(R.string.should_choose_semester),Toast.LENGTH_SHORT).show()
            }
        }
    }
}