package com.bignerdranch.android.todolist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.todolist.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE id=(:id)")
    suspend fun getTask(id: UUID): Task

    @Update
    suspend fun updateTask(task: Task)

    @Insert
    suspend fun addTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE Task SET selectedPriority = :priority WHERE id = :taskId")
    suspend fun updateSelectedPriority(taskId: UUID, priority: Int)

    @Query("UPDATE Task SET selectedCategory = :category WHERE id = :taskId")
    suspend fun updateSelectedCategory(taskId: UUID, category: Int)

    @Query("SELECT * FROM Task WHERE Date <= :currentDate")
    suspend fun getTasksForNotification(currentDate: Long): List<Task>
}
