package com.bignerdranch.android.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.todolist.databinding.FragmentTaskListBinding
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val taskListViewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)

        binding.taskRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_task_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_task -> {
                showNewTask()
                true }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showNewTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newTask = Task(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                isComplete = false,
                priority = "",
                category = "",
                selectedPriority = 0, // Set a default value here
                selectedCategory = 0 // Set a default value here
            )
            taskListViewModel.addTask(newTask)
            findNavController().navigate(
                TaskListFragmentDirections.showTaskDetail(newTask.id)
            )
            updateViewsVisibility(taskListViewModel.tasks.value)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskListViewModel.tasks.collect { tasks ->
                    binding.taskRecyclerView.adapter =
                        TaskListAdapter(tasks) { taskId ->
                            findNavController().navigate(
                                TaskListFragmentDirections.showTaskDetail(taskId))
                        }
                    updateViewsVisibility(tasks)
                }
            }
        }
        binding.newTaskButton.setOnClickListener {
            showNewTask()
        }
    }
    private fun updateViewsVisibility(crimes: List<Task>) {
        if (crimes.isEmpty()) {
            binding.emptyTaskList.visibility = View.VISIBLE
            binding.newTaskButton.visibility = View.VISIBLE
            binding.taskRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTaskList.visibility = View.GONE
            binding.newTaskButton.visibility = View.GONE
            binding.taskRecyclerView.visibility = View.VISIBLE
        }
    }
}
