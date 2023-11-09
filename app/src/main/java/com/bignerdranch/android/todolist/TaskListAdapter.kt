package com.bignerdranch.android.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.todolist.databinding.ListItemTaskBinding
import java.util.UUID

class TaskHolder(
    private val binding: ListItemTaskBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(task: Task, onTaskClicked: (taskId: UUID) -> Unit) {
        binding.taskTitle.text = task.title
        binding.taskDate.text = task.date.toString()

        binding.root.setOnClickListener {
            onTaskClicked(task.id)
        }

        binding.taskComplete.visibility = if (task.isComplete) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

class TaskListAdapter(
    private val tasks: List<Task>,
    private val onTaskClicked: (taskId: UUID) -> Unit
) : RecyclerView.Adapter<TaskHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : TaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemTaskBinding.inflate(inflater, parent, false)
        return TaskHolder(binding)
    }
    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, onTaskClicked)
    }
    override fun getItemCount() = tasks.size
}