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

        // Set the initial query from the ViewModel
        searchView.setQuery(taskListViewModel.getLastSearchQuery(), false)

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

                    taskListViewModel.getFilteredTasks(it, selectedCategory, selectedPriority)

                    // Update the search query in the ViewModel
                    taskListViewModel.setSearchQuery(it)
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

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

                    // Update the selected category in the ViewModel
                    taskListViewModel.setSelectedCategory(selectedCategory)

                    val searchQuery = taskListViewModel.getLastSearchQuery()
                    taskListViewModel.getFilteredTasks(searchQuery, selectedCategory, selectedPriority)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing
            }
        }

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

                    // Update the selected priority in the ViewModel
                    taskListViewModel.setSelectedPriority(selectedPriority)

                    val searchQuery = taskListViewModel.getLastSearchQuery()
                    taskListViewModel.getFilteredTasks(searchQuery, selectedCategory, selectedPriority)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing
            }
        }

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
                                TaskListFragmentDirections.showTaskDetail(taskId))
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
                taskListViewModel.selectedCategoryKey > 0 ||
                taskListViewModel.selectedPriorityKey > 0) {
                // Show other UI elements like spinners, divider, noResultFound, etc.
                Log.d("TaskListFragment", "Here")
                binding.prioritySpinner.visibility = View.VISIBLE
                binding.categorySpinner.visibility = View.VISIBLE
                binding.sessionDivider.visibility = View.VISIBLE
                binding.noResultFound.visibility = View.VISIBLE
                binding.emptyTaskList.visibility = View.GONE
                binding.newTaskButton.visibility = View.GONE
                binding.taskRecyclerView.visibility = View.GONE
            } else {
                Log.d("TaskListFragment", "HereHere")
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
            Log.d("TaskListFragment", "HereHerHere")
            binding.prioritySpinner.visibility = View.VISIBLE
            binding.categorySpinner.visibility = View.VISIBLE
            binding.noResultFound.visibility = View.GONE
            binding.emptyTaskList.visibility = View.GONE
            binding.newTaskButton.visibility = View.GONE
            binding.taskRecyclerView.visibility = View.VISIBLE
        }
    }
}
