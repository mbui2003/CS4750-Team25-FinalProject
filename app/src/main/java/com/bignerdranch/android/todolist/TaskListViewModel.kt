package com.bignerdranch.android.todolist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG = "TaskListViewModel"

class TaskListViewModel : ViewModel() {
    private val taskRepository = TaskRepository.get()

    private val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasks: StateFlow<List<Task>>
        get() = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
        }
    }

    suspend fun addTask(task: Task) {
        Log.d(TAG, "Adding task in ViewModel: $task")
        taskRepository.addTask(task)
        taskRepository.getTasks().collect { tasks ->
            _tasks.value = tasks
        }
    }
}
