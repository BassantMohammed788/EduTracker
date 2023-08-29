package com.example.edutracker.teacher.attendance.view

import GradeLevelAdapter
import android.annotation.SuppressLint
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
import com.example.edutracker.databinding.FragmentAttendanceBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.attendance.viewmodel.AttendanceViewModel
import com.example.edutracker.teacher.attendance.viewmodel.AttendanceViewModelFactory
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.teacher.lessons.view.GroupAdapter
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModel
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModelFactory
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModel
import com.example.edutracker.teacher.students.viewmodel.StudentsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import com.example.edutracker.utilities.gradeLevel
import kotlinx.coroutines.launch


class AttendanceFragment : Fragment() {
    private lateinit var binding: FragmentAttendanceBinding

    private lateinit var studentsViewModel: StudentsViewModel
    private lateinit var studentsViewModelFactory: StudentsViewModelFactory
    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var attendanceViewModelFactory: AttendanceViewModelFactory
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory
    private lateinit var lessonViewModel: LessonsViewModel
    private lateinit var lessonsViewModelFactory: LessonsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter
    private lateinit var groupsAdapter: GroupAdapter
    private lateinit var monthsAdapter: GradeLevelAdapter
    private lateinit var lessonAdapter: GroupLessonsAdapter
    private var gradeVar: String? = null
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var monthVar: String? = null
    private var teacherIdVar: String? = null
    private var lessonVar: String? = null
    private var lessonIdVar : String? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studentsViewModelFactory = StudentsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        studentsViewModel = ViewModelProvider(this, studentsViewModelFactory)[StudentsViewModel::class.java]

        attendanceViewModelFactory = AttendanceViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        attendanceViewModel = ViewModelProvider(this, attendanceViewModelFactory)[AttendanceViewModel::class.java]
        groupsViewModelFactory = GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        groupsViewModel = ViewModelProvider(this, groupsViewModelFactory)[GroupsViewModel::class.java]
        lessonsViewModelFactory = LessonsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        lessonViewModel = ViewModelProvider(this, lessonsViewModelFactory)[LessonsViewModel::class.java]

        teacherIdVar = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!

        gradeLevelAdapter = GradeLevelAdapter(emptyList(),gradeLambda)
        groupsAdapter=GroupAdapter(groupClickLambda)
        monthsAdapter=GradeLevelAdapter (emptyList(),monthClickLambda)
        lessonAdapter= GroupLessonsAdapter (emptyList(),lessonClickLambda)
        binding.chooseLevel.setOnClickListener{
            if (semesterVar!=null){
                displayGradeLevelDialog()
            }else{
                Toast.makeText(requireContext(), getString(R.string.you_should_choose_semester), Toast.LENGTH_SHORT).show()
            }
        }

        binding.chooseGroup.setOnClickListener{
            if (gradeVar!=null){
                displayGroupDialog()
            }else{
                Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level), Toast.LENGTH_SHORT).show()
            }
        }
        binding.chooseMonth.setOnClickListener {
            if (gradeVar!=null&&groupNameVar!=null){
                displayMonthDialog()
            }else{
                Toast.makeText(requireContext(), getString(R.string.you_should_choose_grade_level_and_group), Toast.LENGTH_SHORT).show()
            }
        }
        binding.chooseLesson.setOnClickListener{
            if (monthVar!=null){
                displayLessonDialog()
            }else{
                Toast.makeText(requireContext(), getString(R.string.you_should_choose_month), Toast.LENGTH_SHORT).show()
            }
        }
        binding.takeAttendanceButton.setOnClickListener{

            if (monthVar!=null) {
                val view = view
                val action =  //AttendanceFragmentDirections.actionAttendanceFragmentToAllLessonsFragment()
                   AttendanceFragmentDirections.actionAttendanceFragmentToTakeAttendanceFragment(lessonIdVar!!,groupIdVar!!,monthVar!!,gradeVar!!,groupNameVar!!)
                    Navigation.findNavController(view).navigate(action)
            }

        }


    }
    private fun displayGradeLevelDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = gradeLevelAdapter
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                groupsViewModel.getAllGrades(semesterVar!!, MySharedPreferences.getInstance(requireContext()).getTeacherID()!!)
                groupsViewModel.getGrades.collect{ result->
                    when(result){
                        is FirebaseState.Loading ->{
                            gradeLevelDialog.progressBar.visibility=View.VISIBLE
                            gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE
                        }
                        is FirebaseState.Success ->{
                            if (result.data.isEmpty()){
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE

                                gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.VISIBLE
                                gradeLevelDialog.noDataTv.text=getString(R.string.no_grades_yet)
                            }else{
                                gradeLevelDialog.progressBar.visibility=View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                val list = mutableListOf<String>()
                                for (i in result.data){
                                    list.add(gradeLevel(i)!!)
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
            binding.gradeName.text = gradeVar
            dialog.dismiss()
        }
    }
    private fun displayGroupDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = groupsAdapter
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_group)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            if (gradeLevel(gradeVar) != null){
                lifecycleScope.launch {
                    groupsViewModel.getAllGroups(semesterVar!!,MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                        gradeLevel(gradeVar)!!)
                    groupsViewModel.getGroups.collect{ result->
                        when(result){
                            is FirebaseState.Loading ->{
                                gradeLevelDialog.progressBar.visibility=View.VISIBLE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE
                            }
                            is FirebaseState.Success ->{
                                if (result.data.isEmpty()){
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                    gradeLevelDialog.noDataTv.text=getString(R.string.no_groups_yet)
                                    gradeLevelDialog.noDataTv.visibility=View.VISIBLE
                                }else{
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                    groupsAdapter.submitList(result.data)
                                }

                            }
                            is FirebaseState.Failure ->{}

                        }
                    }
                }
            }else{
                Toast.makeText(requireContext(),getString(R.string.you_should_choose_semester_level),Toast.LENGTH_LONG).show()
            }}else{
            binding.constraintLayout.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            binding.groupName.text = groupNameVar
            dialog.dismiss()
        }
    }

    private fun displayMonthDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val monthDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        monthDialog.GradeLevelRecycler.adapter = monthsAdapter
        monthDialog.GradeTv.text=getString(R.string.choose_month)
        builder.setView(monthDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())){
            lifecycleScope.launch {
                lessonViewModel.getAllMonths(semesterVar!!, MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                    gradeLevel(gradeVar!!)!!,groupIdVar!!)
                lessonViewModel.getAllMonths.collect{ result->
                    when(result){
                        is FirebaseState.Loading ->{
                            monthDialog.progressBar.visibility=View.VISIBLE
                            monthDialog.noDataTv.visibility=View.INVISIBLE
                            monthDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success ->{
                            if (result.data.isEmpty()){
                                monthDialog.progressBar.visibility=View.INVISIBLE
                                monthDialog.GradeLevelRecycler.visibility=View.GONE
                                monthDialog.noDataTv.visibility=View.VISIBLE
                                monthDialog.noDataTv.text=getString(R.string.no_months_yet)
                            }else{
                                monthDialog.noDataTv.visibility=View.INVISIBLE
                                monthDialog.progressBar.visibility=View.GONE
                                monthDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                val list = mutableListOf<String>()
                                val englishMonthList = listOf(
                                    "January", "February", "March", "April", "May", "June",
                                    "July", "August", "September", "October", "November", "December"
                                )
                                for (i in result.data){
                                    if (i in englishMonthList) {
                                        list.add(i)
                                    }
                                }
                                monthsAdapter.setGradeLevelsList(list)
                            }

                        }
                        is FirebaseState.Failure ->{}

                    }

                }
            }
        }else{
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        monthDialog.okBTN.setOnClickListener {
            binding.monthName.text = monthVar
            dialog.dismiss()
        }
    }
    private fun displayLessonDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = lessonAdapter
        gradeLevelDialog.GradeTv.text=getString(R.string.choose_lesson)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (monthVar!=null){
            if (checkConnectivity(requireContext())){
                lifecycleScope.launch {
                    lessonViewModel.getAllLessons(teacherIdVar!!,semesterVar!!, gradeLevel(gradeVar!!)!!,groupIdVar!!,monthVar!!)
                    lessonViewModel.getAllLessons.collect{ result->
                        when(result){
                            is FirebaseState.Loading->{
                                gradeLevelDialog.progressBar.visibility=View.VISIBLE
                                gradeLevelDialog.GradeLevelRecycler.visibility=View.GONE
                            }
                            is FirebaseState.Success ->{
                                if (result.data.isEmpty()){
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.INVISIBLE
                                    gradeLevelDialog.noDataTv.text=getString(R.string.no_groups_yet)
                                    gradeLevelDialog.noDataTv.visibility=View.VISIBLE

                                }else{
                                    gradeLevelDialog.progressBar.visibility=View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                    lessonAdapter.setGradeLevelsList(result.data)
                                }

                            }
                            is FirebaseState.Failure ->{}

                        }
                    }
                }
            }else{
                Toast.makeText(requireContext(),getString(R.string.you_should_choose_semester_level),Toast.LENGTH_LONG).show()
            }}else{
            binding.constraintLayout.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.network_lost_title), Toast.LENGTH_SHORT).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            binding.LessonName.text = lessonVar
            dialog.dismiss()
        }
    }

    private val gradeLambda = { string: String ->
        gradeVar = string
    }

    private val groupClickLambda = { group: Group ->

        groupNameVar = group.name
        Log.i("TAG", ":$groupNameVar ")
        groupIdVar = group.id
    }
    private val monthClickLambda = { string: String ->

        Log.i("TAG", ":${ string } ")
        monthVar = string

    }
    private val lessonClickLambda = { lesson: Lesson ->
        Log.i("TAG", "lesson :${lesson} ")
        lessonVar = lesson.time
        lessonIdVar= lesson.id

    }

}