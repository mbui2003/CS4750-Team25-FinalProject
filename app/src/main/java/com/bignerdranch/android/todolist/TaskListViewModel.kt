package com.bignerdranch.android.todolist

import android.util.Log
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

    private val _searchKey = "search_query"

    // Save state if the user is sorting
    private val _isSorting = MutableStateFlow(false)
    val isSorting: StateFlow<Boolean>
        get() = _isSorting.asStateFlow()

    init {
        viewModelScope.launch {
            // Retrieve the last search query from SavedStateHandle
            val lastSearchQuery: String? = savedStateHandle.get(_searchKey)
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
            savedStateHandle.set(_searchKey, searchQuery)
            taskRepository.searchTasksByName(searchQuery).collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun getLastSearchQuery(): String {
        return savedStateHandle.get(_searchKey) ?: ""
    }

    fun getTasksByCategory(category: Int) {
        viewModelScope.launch {
            taskRepository.getTasksByCategory(category).collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun getTasksByPriority(priority: Int) {
        viewModelScope.launch {
            taskRepository.getTasksByPriority(priority).collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun setIsSorting(isSorting: Boolean) {
        _isSorting.value = isSorting
        Log.d(TAG, "SetIsSorting reached: $isSorting")
    }
}
