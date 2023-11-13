package com.bignerdranch.android.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "TaskListViewModel"

class TaskListViewModel : ViewModel() {
    private val taskRepository = TaskRepository.get()

    private val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasks: StateFlow<List<Task>>
        get() = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getTasks().collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    suspend fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

}
