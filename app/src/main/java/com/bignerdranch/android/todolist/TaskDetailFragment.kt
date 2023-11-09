package com.bignerdranch.android.todolist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.util.UUID

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
                    task?.let { updateUi(it) }
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

    private fun updateUi(task: Task) {
        binding.apply {
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