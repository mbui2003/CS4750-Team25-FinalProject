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

    private val _searchKey = "search_query"

    private val _selectedPriorityKey = "selectedPriority"
    val selectedPriorityKey: Int
        get() = savedStateHandle.get(_selectedPriorityKey) ?: 0

    private val _selectedCategoryKey = "selectedCategory"
    val selectedCategoryKey: Int
        get() = savedStateHandle.get(_selectedCategoryKey) ?: 0

    // Save state if the user is sorting
    private val _isSortingLoading = MutableStateFlow(false)
    val isSortingLoading: StateFlow<Boolean>
        get() = _isSortingLoading.asStateFlow()

    fun setIsSortingLoading(isLoading: Boolean) {
        _isSortingLoading.value = isLoading
    }

    init {
        viewModelScope.launch {
            val lastSearchQuery: String? = savedStateHandle.get(_searchKey)
            val lastSelectedCategory: Int = savedStateHandle.get(_selectedCategoryKey) ?: 0
            val lastSelectedPriority: Int = savedStateHandle.get(_selectedPriorityKey) ?: 0

            // Use the last search query, selected category, and priority to initialize the list of tasks
            taskRepository.getFilteredTasks(lastSearchQuery ?: "", lastSelectedCategory, lastSelectedPriority)
                .collect { tasks ->
                    _tasks.value = tasks
                }
        }
    }

    suspend fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

    fun getLastSearchQuery(): String {
        return savedStateHandle.get(_searchKey) ?: ""
    }

    fun setSearchQuery(query: String) {
        savedStateHandle.set(_searchKey, query)
    }

    fun getFilteredTasks(searchQuery: String, category: Int, priority: Int) {
        setIsSortingLoading(true)
        viewModelScope.launch {
            savedStateHandle.set(_searchKey, searchQuery)
            taskRepository.getFilteredTasks(searchQuery, category, priority).collect { tasks ->
                _tasks.value = tasks
            }
        }
        setIsSortingLoading(false)
    }

    fun setSelectedPriority(selectedPriority: Int) {
        savedStateHandle.set(_selectedPriorityKey, selectedPriority)
    }

    fun setSelectedCategory(selectedCategory: Int) {
        savedStateHandle.set(_selectedCategoryKey, selectedCategory)
    }
}
