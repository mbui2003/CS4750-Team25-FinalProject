package com.bignerdranch.android.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.todolist.databinding.FragmentTaskDetailBinding
import kotlinx.coroutines.launch
import java.util.Date

class TaskDetailFragment: Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: TaskDetailFragmentArgs by navArgs()

    private val taskDetailViewModel: TaskDetailViewModel by viewModels {
        TaskDetailViewModelFactory(args.taskId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentTaskDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            taskTitle.doOnTextChanged { text, _, _, _ ->
                taskDetailViewModel.updateTask { oldTask ->
                    oldTask.copy(title = text.toString())
                }
            }

            taskComplete.setOnCheckedChangeListener { _, isChecked ->
                taskDetailViewModel.updateTask { oldTask ->
                    oldTask.copy(isComplete = isChecked)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskDetailViewModel.task.collect { task ->
                    task?.let {
                        updateUi(it)

                        val priorityArray = resources.getStringArray(R.array.priority_levels)
                        binding.prioritySpinner.setSelection(task.selectedPriority)

                        val categoryArray = resources.getStringArray(R.array.category_levels)
                        binding.categorySpinner.setSelection(task.selectedCategory)

                        binding.prioritySpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                                taskDetailViewModel.updateTask { oldTask ->
                                    oldTask.copy(selectedPriority = position)
                                }
                            }

                            override fun onNothingSelected(parentView: AdapterView<*>) {
                                // Do nothing
                            }
                        })

                        binding.categorySpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                                taskDetailViewModel.updateTask { oldTask ->
                                    oldTask.copy(selectedCategory = position)
                                }
                            }

                            override fun onNothingSelected(parentView: AdapterView<*>) {
                                // Do nothing
                            }
                        })
                    }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            taskDetailViewModel.updateTask { it.copy(date = newDate) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_task_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_task -> {
                deleteTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private var currentTask: Task? = null

    private fun deleteTask() {
        val taskToDelete = currentTask
        if (taskToDelete != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                taskDetailViewModel.deleteTask(taskToDelete)
                findNavController().navigateUp()
            }
        }
    }

    private fun updateUi(task: Task) {
        binding.apply {
            currentTask = task

            if (taskTitle.text.toString() != task.title) {
                taskTitle.setText(task.title)
            }
            taskDate.text = task.date.toString()
            taskDate.setOnClickListener {
                findNavController().navigate(
                    TaskDetailFragmentDirections.selectDate(task.date)
                ) }
            taskComplete.isChecked = task.isComplete
        }
    }
}