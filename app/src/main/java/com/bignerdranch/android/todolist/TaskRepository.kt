package com.bignerdranch.android.todolist

import android.content.Context
import androidx.room.Room
import com.bignerdranch.android.todolist.database.TaskDatabase
import com.bignerdranch.android.todolist.database.migration_1_2
import com.bignerdranch.android.todolist.database.migration_2_3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "task-database"

class TaskRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {

    private val sharedPreferences =
        context.getSharedPreferences("TaskRepository", Context.MODE_PRIVATE)

    private val database: TaskDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
        .addMigrations(migration_1_2, migration_2_3)
        .build()

    fun getTasks(): Flow<List<Task>> = database.taskDAO().getTasks()

    suspend fun getTask(id: UUID): Task = database.taskDAO().getTask(id)

    fun updateTask(task: Task) {
        coroutineScope.launch {
            database.taskDAO().updateTask(task)
        }
    }

    suspend fun addTask(task: Task) {
        database.taskDAO().addTask(task)
    }

    suspend fun deleteTask(task: Task) {
        database.taskDAO().deleteTask(task)
    }

    companion object {
        private var INSTANCE: TaskRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TaskRepository(context)
            }
        }
        fun get(): TaskRepository {
            return INSTANCE ?:
            throw IllegalStateException("TaskRepository must be initialized")
        }
    }

    suspend fun getTasksForNotification(currentDate: Long): List<Task> {
        return database.taskDAO().getTasksForNotification(currentDate)
    }

    fun searchTasksByName(searchQuery: String): Flow<List<Task>> {
        return database.taskDAO().searchTasksByName(searchQuery)
    }

    fun getTasksByCategory(category: Int): Flow<List<Task>> {
        return database.taskDAO().getTasksByCategory(category)
    }

    fun getTasksByPriority(priority: Int): Flow<List<Task>> {
        return database.taskDAO().getTasksByPriority(priority)
    }

}
