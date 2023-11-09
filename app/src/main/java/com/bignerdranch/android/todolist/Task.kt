package com.bignerdranch.android.todolist

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Task(
    @PrimaryKey val id: UUID,
    val title: String,
    val date: Date,
    val isComplete: Boolean
)
