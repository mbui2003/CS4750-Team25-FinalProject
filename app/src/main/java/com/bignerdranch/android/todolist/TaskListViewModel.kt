package com.bignerdranch.android.todolist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "TaskListViewModel"

class TaskListViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val taskRepository = TaskRepository.get()

    private val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasks: StateFlow<List<Task>>
        get() = _tasks.asStateFlow()

    private val SEARCH_QUERY_KEY = "search_query"

    init {
        viewModelScope.launch {
            // Retrieve the last search query from SavedStateHandle
            val lastSearchQuery: String? = savedStateHandle.get(SEARCH_QUERY_KEY)
            // Use the last search query to initialize the list of tasks
            taskRepository.searchTasksByName(lastSearchQuery ?: "").collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    suspend fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

    fun searchTasksByName(searchQuery: String) {
        viewModelScope.launch {
            savedStateHandle.set(SEARCH_QUERY_KEY, searchQuery)
            taskRepository.searchTasksByName(searchQuery).collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun getLastSearchQuery(): String {
        return savedStateHandle.get(SEARCH_QUERY_KEY) ?: ""
    }
}
