package com.example.edutracker.teacher.lessons.view

import GradeLevelAdapter
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import com.example.edutracker.databinding.FragmentAddLessonBinding
import com.example.edutracker.databinding.GradeLevelDialogBinding
import com.example.edutracker.databinding.LoadingDialogBinding
import com.example.edutracker.dataclasses.Group
import com.example.edutracker.dataclasses.Lesson
import com.example.edutracker.models.Repository
import com.example.edutracker.network.FirebaseState
import com.example.edutracker.network.RemoteClient
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModel
import com.example.edutracker.teacher.groups.viewmodel.GroupsViewModelFactory
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModel
import com.example.edutracker.teacher.lessons.viewmodel.LessonsViewModelFactory
import com.example.edutracker.utilities.MySharedPreferences
import com.example.edutracker.utilities.checkConnectivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddLessonFragment : Fragment() {
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsViewModelFactory: GroupsViewModelFactory
    private lateinit var lessonViewModel: LessonsViewModel
    private lateinit var lessonsViewModelFactory: LessonsViewModelFactory
    private lateinit var gradeLevelAdapter: GradeLevelAdapter
    private var gradeVar: String? = null
    private lateinit var groupsAdapter: GroupAdapter
    private var semesterVar: String? = null
    private var groupNameVar: String? = null
    private var groupIdVar: String? = null
    private var selectedMonthString: String? = null
    private var selectedDateTimeString: String? = null

    lateinit var binding: FragmentAddLessonBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddLessonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupsViewModelFactory =
            GroupsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        groupsViewModel =
            ViewModelProvider(this, groupsViewModelFactory)[GroupsViewModel::class.java]
        lessonsViewModelFactory =
            LessonsViewModelFactory(Repository.getInstance(RemoteClient.getInstance()))
        lessonViewModel =
            ViewModelProvider(this, lessonsViewModelFactory)[LessonsViewModel::class.java]
        gradeLevelAdapter = GradeLevelAdapter(emptyList(), gradeLambda)
        val teacherId = MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
        semesterVar = MySharedPreferences.getInstance(requireContext()).getSemester()!!
        groupsAdapter = GroupAdapter(groupClickLambda)
        binding.chooseLevel.setOnClickListener {
            if (semesterVar != null) {
                displayGradeLevelDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_semester),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.chooseGroup.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_grade_level),
                    Toast.LENGTH_SHORT
                ).show()
        }
        binding.chooseDate.setOnClickListener {
            showDateTimePicker(requireContext())
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(requireView()).apply {
                popBackStack()
            }
        }

        binding.AddButton.setOnClickListener {
            if (selectedDateTimeString != null && gradeVar != null && groupNameVar != null) {
                if (checkConnectivity(requireContext())) {
                    val lesson = Lesson(
                        UUID.randomUUID().toString(),
                        groupIdVar!!,
                        teacherId,
                        selectedDateTimeString!!
                    )
                    lifecycleScope.launch {
                        val builder = AlertDialog.Builder(requireContext())
                        val loadingDialogB = LoadingDialogBinding.inflate(layoutInflater)
                        builder.setView(loadingDialogB.root)
                        val dialog = builder.create()
                        dialog.setCancelable(false)
                        lessonViewModel.addLesson(teacherId, semesterVar!!, gradeVar!!, selectedMonthString!!, lesson)
                        lessonViewModel.addLesson.collect { result ->
                            when (result) {
                                is FirebaseState.Loading -> {
                                    dialog.show()
                                    binding.allLinear.visibility = View.INVISIBLE
                                }
                                is FirebaseState.Success -> {
                                    dialog.dismiss()
                                    binding.allLinear.visibility = View.VISIBLE
                                    if (result.data) {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.added_successfully),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Navigation.findNavController(requireView()).apply {
                                            popBackStack()
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "The Lesson already exists",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                is FirebaseState.Failure -> {
                                    dialog.dismiss()
                                    Log.i("TAG", "onViewCreated: failed")
                                }

                            }
                        }
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.network_lost_title),
                        Toast.LENGTH_SHORT
                    ).show()

                }

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_time),
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }


    private val gradeLambda = { string: String ->
        gradeVar = string
        binding.gradeName.text = gradeVar
    }

    private val groupClickLambda = { group: Group ->
        Log.i("TAG", "groupVar:$groupNameVar ")
        binding.groupName.text = groupNameVar
        groupIdVar = group.id
        groupNameVar = group.name
    }

    private fun displayGradeLevelDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = gradeLevelAdapter
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())) {
            lifecycleScope.launch {
                groupsViewModel.getAllGrades(
                    semesterVar!!,
                    MySharedPreferences.getInstance(requireContext()).getTeacherID()!!
                )
                groupsViewModel.getGrades.collect { result ->
                    when (result) {
                        is FirebaseState.Loading -> {
                            gradeLevelDialog.progressBar.visibility = View.VISIBLE
                            gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE

                            gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                            gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                        }
                        is FirebaseState.Success -> {
                            if (result.data.isEmpty()) {
                                gradeLevelDialog.progressBar.visibility = View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.INVISIBLE

                                gradeLevelDialog.noDataAnimationView.visibility=View.VISIBLE
                                gradeLevelDialog.noDataTv.visibility = View.VISIBLE
                                gradeLevelDialog.noDataTv.text = getString(R.string.no_grades_yet)
                            } else {
                                gradeLevelDialog.progressBar.visibility = View.GONE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.VISIBLE
                                gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                val list = mutableListOf<String>()
                                for (i in result.data) {
                                    list.add(i)
                                }
                                gradeLevelAdapter.setGradeLevelsList(list)
                            }

                        }
                        is FirebaseState.Failure -> {}

                    }

                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.network_lost_title),
                Toast.LENGTH_SHORT
            ).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            if (gradeVar!=null) {
                binding.gradeName.text = gradeVar
                displayGroupDialog()
            }
            dialog.dismiss()
        }
    }
    private fun displayGroupDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val gradeLevelDialog = GradeLevelDialogBinding.inflate(layoutInflater)
        gradeLevelDialog.GradeLevelRecycler.adapter = groupsAdapter
        gradeLevelDialog.GradeTv.text = getString(R.string.choose_group)
        builder.setView(gradeLevelDialog.root)
        val dialog = builder.create()
        dialog.show()
        if (checkConnectivity(requireContext())) {
            if (gradeVar != null) {
                lifecycleScope.launch {
                    groupsViewModel.getAllGroups(
                        semesterVar!!,
                        MySharedPreferences.getInstance(requireContext()).getTeacherID()!!,
                        gradeVar!!
                    )
                    groupsViewModel.getGroups.collect { result ->
                        when (result) {
                            is FirebaseState.Loading -> {
                                gradeLevelDialog.progressBar.visibility = View.VISIBLE
                                gradeLevelDialog.GradeLevelRecycler.visibility = View.GONE

                                gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                            }
                            is FirebaseState.Success -> {
                                if (result.data.isEmpty()) {
                                    gradeLevelDialog.progressBar.visibility = View.GONE
                                    gradeLevelDialog.GradeLevelRecycler.visibility = View.INVISIBLE
                                    gradeLevelDialog.noDataTv.visibility = View.VISIBLE
                                    gradeLevelDialog.noDataAnimationView.visibility=View.VISIBLE
                                    gradeLevelDialog.noDataTv.text =
                                        getString(R.string.no_groups_yet)
                                } else {
                                    gradeLevelDialog.progressBar.visibility = View.GONE
                                    gradeLevelDialog.noDataAnimationView.visibility=View.INVISIBLE
                                    gradeLevelDialog.noDataTv.visibility=View.INVISIBLE
                                    gradeLevelDialog.GradeLevelRecycler.visibility = View.VISIBLE

                                    groupsAdapter.submitList(result.data)
                                }

                            }
                            is FirebaseState.Failure -> {}

                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_should_choose_semester_level),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.network_lost_title),
                Toast.LENGTH_SHORT
            ).show()
        }
        gradeLevelDialog.okBTN.setOnClickListener {
            if (groupNameVar!=null) {
                binding.groupName.text = groupNameVar
            }
            dialog.dismiss()
        }
    }

    private fun showDateTimePicker(context: Context) {

        val arabicLocale = Locale("ar")

        val calendar = Calendar.getInstance(arabicLocale)
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance(arabicLocale)
                selectedDate.set(year, month, dayOfMonth)
                val chosenMonth =
                    SimpleDateFormat("MMMM", arabicLocale).format(selectedDate.time)
                val years = SimpleDateFormat("yyyy", arabicLocale).format(selectedDate.time)
                selectedMonthString = chosenMonth
                Log.i("TAG", "year: $years")
                Log.i("TAG", "month: $selectedMonthString")

                // Time Picker Dialog
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        // Format the selected date and time
                        val selectedTimeCalendar = Calendar.getInstance(arabicLocale)
                        selectedTimeCalendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        val selectedDateTime = selectedTimeCalendar.time
                        val dateTimeFormat =
                            SimpleDateFormat("EEEE, d MMMM yyyy, h:mm a", arabicLocale)
                        selectedDateTimeString = dateTimeFormat.format(selectedDateTime)
                        binding.dateName.text = selectedDateTimeString

                    },
                    currentHour,
                    currentMinute,
                    false
                )

                timePickerDialog.show()

            },
            currentYear,
            currentMonth,
            currentDayOfMonth
        )

        datePickerDialog.show()
    }



}