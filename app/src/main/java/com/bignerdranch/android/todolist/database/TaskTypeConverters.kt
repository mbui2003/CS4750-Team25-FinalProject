package com.bignerdranch.android.todolist.database

import androidx.room.TypeConverter
import java.util.Date

class TaskTypeConverters {

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}