package com.bignerdranch.android.todolist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
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

        val categorySpinner: Spinner = binding.categorySpinner
        val prioritySpinner: Spinner = binding.prioritySpinner

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

        val searchItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Close keyboard and clear cursor when hit enter
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val selectedPriority = binding.prioritySpinner.selectedItemPosition
                    val selectedCategory = binding.categorySpinner.selectedItemPosition

                    if (it.isNotEmpty()) {
                        if (selectedPriority > 0 && selectedCategory > 0) {
                            taskListViewModel.getTasksByPriority(selectedPriority)
                            taskListViewModel.getTasksByCategory(selectedCategory)
                        } else {
                            if (selectedPriority > 0) {
                                taskListViewModel.getTasksByPriority(selectedPriority)
                            } else {
                                taskListViewModel.getTasksByCategory(selectedCategory)
                            }
                        }

                        taskListViewModel.searchTasksByName(it)
                    } else {
                        if (selectedPriority > 0 && selectedCategory > 0) {
                            taskListViewModel.getTasksByPriority(selectedPriority)
                            taskListViewModel.getTasksByCategory(selectedCategory)
                        } else {
                            if (selectedPriority > 0) {
                                taskListViewModel.getTasksByPriority(selectedPriority)
                            } else {
                                taskListViewModel.getTasksByCategory(selectedCategory)
                            }
                        }
                    }
                }
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_task -> {
                showNewTask()
                true
            }

            //  else -> super.onOptionsItemSelected(item)
            else -> true
        }
    }

    private fun showNewTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newTask = Task(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                isComplete = false,
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

        // Priority and Spinner Code session
        val categorySpinner: Spinner = binding.categorySpinner
        val prioritySpinner: Spinner = binding.prioritySpinner

        val categoryAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.category_levels,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val priorityAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_levels,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }


        categorySpinner.adapter = categoryAdapter
        prioritySpinner.adapter = priorityAdapter

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedPriority = prioritySpinner.selectedItemPosition
                val selectedCategory = categorySpinner.selectedItemPosition

                viewLifecycleOwner.lifecycleScope.launch {
                    Log.d("TaskListFragment", "Selected Priority: $selectedPriority")
                    Log.d("TaskListFragment", "Selected Category: $selectedCategory")
                    if (selectedPriority > 0) {
                        taskListViewModel.searchTasksByName(taskListViewModel.getLastSearchQuery())
                        taskListViewModel.getTasksByPriority(selectedPriority)
                        taskListViewModel.setIsPrioritySorting(true)
                    } else {
                        if (selectedCategory > 0) {
                            Log.d("TaskListFragment", "Right here priority")
                            taskListViewModel.searchTasksByName(taskListViewModel.getLastSearchQuery())
                            taskListViewModel.getTasksByCategory(categorySpinner.selectedItemPosition)
                        } else {
                            // User selected the default option, fetch the original list
                            Log.d("TaskListFragment", "Right here here priority")
                            taskListViewModel.searchTasksByName(taskListViewModel.getLastSearchQuery())
                            taskListViewModel.setIsPrioritySorting(false)
                        }
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing
            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedPriority = prioritySpinner.selectedItemPosition
                val selectedCategory = categorySpinner.selectedItemPosition
                Log.d("TaskListFragment", "Selected Priority: $selectedPriority")
                Log.d("TaskListFragment", "Selected Category: $selectedCategory")

                viewLifecycleOwner.lifecycleScope.launch {
                    if (selectedCategory > 0) {
                        taskListViewModel.searchTasksByName(taskListViewModel.getLastSearchQuery())
                        taskListViewModel.getTasksByCategory(selectedCategory)
                        taskListViewModel.setIsCategorySorting(true)
                    } else {
                        if (selectedPriority > 0) {
                            Log.d("TaskListFragment", "Right here category")
                            taskListViewModel.searchTasksByName(taskListViewModel.getLastSearchQuery())
                            taskListViewModel.getTasksByPriority(prioritySpinner.selectedItemPosition)
                        } else {
                            // Fetch the original list if the user selected the default option,
                            Log.d("TaskListFragment", "Right here here category")
                            taskListViewModel.searchTasksByName(taskListViewModel.getLastSearchQuery())
                            taskListViewModel.setIsCategorySorting(false)
                        }
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing
            }
        }

        //  viewLifecycleOwner.lifecycleScope.launch {
        //      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        //          taskListViewModel.tasks.collect { tasks ->
        //              binding.taskRecyclerView.adapter =
        //                  TaskListAdapter(tasks) { taskId ->
        //                      findNavController().navigate(
        //                          TaskListFragmentDirections.showTaskDetail(taskId)
        //                      )
        //                  }
        //              updateViewsVisibility(tasks)
        //          }
        //      }
        //  }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Start loading
                taskListViewModel.setIsSortingLoading(true)

                // Collect isSortingLoading separately
                launch {
                    taskListViewModel.isSortingLoading.collect { isSortingLoading ->
                        // Update loading indicator visibility
                        binding.loadingIndicator.isVisible = isSortingLoading
                    }
                }

                // Collect tasks
                taskListViewModel.tasks.collect { tasks ->
                    binding.taskRecyclerView.adapter =
                        TaskListAdapter(tasks) { taskId ->
                            findNavController().navigate(
                                TaskListFragmentDirections.showTaskDetail(taskId)
                            )
                        }

                    updateViewsVisibility(tasks)

                    // End loading
                    taskListViewModel.setIsSortingLoading(false)
                }
            }
        }


        binding.newTaskButton.setOnClickListener {
            showNewTask()
        }
    }

    private fun updateViewsVisibility(tasks: List<Task>) {

        if (tasks.isEmpty()) {
            if (taskListViewModel.getLastSearchQuery().isNotEmpty() ||
                taskListViewModel.isCategorySorting.value ||
                taskListViewModel.isPrioritySorting.value
            ) {
                // Show other UI elements like spinners, divider, noResultFound, etc.
                binding.prioritySpinner.visibility = View.VISIBLE
                binding.categorySpinner.visibility = View.VISIBLE
                binding.sessionDivider.visibility = View.VISIBLE
                binding.noResultFound.visibility = View.VISIBLE
                binding.emptyTaskList.visibility = View.GONE
                binding.newTaskButton.visibility = View.GONE
                binding.taskRecyclerView.visibility = View.GONE
            } else {
                // Show UI elements for an empty list
                binding.prioritySpinner.visibility = View.GONE
                binding.categorySpinner.visibility = View.GONE
                binding.sessionDivider.visibility = View.GONE
                binding.noResultFound.visibility = View.GONE
                binding.emptyTaskList.visibility = View.VISIBLE
                binding.newTaskButton.visibility = View.VISIBLE
                binding.taskRecyclerView.visibility = View.GONE
            }
        } else {
            // Show UI elements for non-empty list
            binding.prioritySpinner.visibility = View.VISIBLE
            binding.categorySpinner.visibility = View.VISIBLE
            binding.noResultFound.visibility = View.GONE
            binding.emptyTaskList.visibility = View.GONE
            binding.newTaskButton.visibility = View.GONE
            binding.taskRecyclerView.visibility = View.VISIBLE
        }
    }
}
