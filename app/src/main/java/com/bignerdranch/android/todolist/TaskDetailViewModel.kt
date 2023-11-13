package com.bignerdranch.android.todolist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TaskDetailViewModel(taskId: UUID) : ViewModel() {
    private val taskRepository = TaskRepository.get()
    private val _task: MutableStateFlow<Task?> = MutableStateFlow(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    init {
        viewModelScope.launch {
            _task.value = taskRepository.getTask(taskId)
        }
    }

    suspend fun deleteTask(task: Task) {
        taskRepository.deleteTask(task)
    }

    fun updateTask(onUpdate: (Task) -> Task) {
        _task.update { oldTask ->
            oldTask?.let { onUpdate(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
            task.value?.let { taskRepository.updateTask(it) }
    }
}

class TaskDetailViewModelFactory(
    private val taskId: UUID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskDetailViewModel(taskId) as T
    }
}