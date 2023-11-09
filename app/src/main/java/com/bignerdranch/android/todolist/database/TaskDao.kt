package com.bignerdranch.android.todolist.database

import androidx.room.Dao
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
}